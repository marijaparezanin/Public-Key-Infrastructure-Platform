package com.ftn.pki.utils.certificates;

import com.ftn.pki.models.certificates.CertificateType;
import com.ftn.pki.models.certificates.Issuer;
import com.ftn.pki.models.certificates.Subject;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.function.BiConsumer;

@Component
public class CertificateUtils {
    public CertificateUtils() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static X509Certificate generateCertificate(Subject subject,
                                                      Issuer issuer,
                                                      Date startDate,
                                                      Date endDate,
                                                      String serialNumber,
                                                      CertificateType type,
                                                      Map<String, String> extensions
    ) {
        try {
            JcaContentSignerBuilder builder = new JcaContentSignerBuilder("SHA256WithRSAEncryption");
            builder = builder.setProvider("BC");

            ContentSigner contentSigner = builder.build(issuer.getPrivateKey());

            X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(issuer.getX500Name(),
                    new BigInteger(serialNumber),
                    startDate,
                    endDate,
                    subject.getX500Name(),
                    subject.getPublicKey());

            if (type == CertificateType.ROOT || type == CertificateType.INTERMEDIATE) {
                certGen.addExtension(
                        Extension.basicConstraints,
                        true,
                        new BasicConstraints(true) // For CA
                );
            } else {
                certGen.addExtension(
                        Extension.basicConstraints,
                        true,
                        new BasicConstraints(false) // not CA
                );
            }

            addExtensions(certGen, extensions, issuer.getPublicKey(), subject.getPublicKey());

            X509CertificateHolder certHolder = certGen.build(contentSigner);

            JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter();
            certConverter = certConverter.setProvider("BC");

            return certConverter.getCertificate(certHolder);

        } catch (IllegalArgumentException | IllegalStateException | OperatorCreationException | CertificateException e) {
            e.printStackTrace();
        } catch (CertIOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static final Map<String, BiConsumer<X509v3CertificateBuilder, Object>> EXTENSION_HANDLERS =
            Map.of(
            // Key Usage
            "2.5.29.15", (builder, value) -> {
                String strValue = (String) value;
                int usage = 0;
                if (strValue.contains("digitalSignature")) usage |= KeyUsage.digitalSignature;
                if (strValue.contains("keyEncipherment")) usage |= KeyUsage.keyEncipherment;
                if (strValue.contains("dataEncipherment")) usage |= KeyUsage.dataEncipherment;
                if (strValue.contains("keyCertSign")) usage |= KeyUsage.keyCertSign;
                if (strValue.contains("cRLSign")) usage |= KeyUsage.cRLSign;

                try {
                    builder.addExtension(
                            new ASN1ObjectIdentifier("2.5.29.15"),
                            true,
                            new KeyUsage(usage)
                    );
                } catch (CertIOException e) {
                    throw new RuntimeException(e);
                }
            },

            // Extended Key Usage
            "2.5.29.37", (builder, value) -> {
                try {
                    String strValue = (String) value;
                    KeyPurposeId[] purposes = Arrays.stream(strValue.split(","))
                            .map(String::trim)
                            .map(s -> switch (s) {
                                case "serverAuth" -> KeyPurposeId.id_kp_serverAuth;
                                case "clientAuth" -> KeyPurposeId.id_kp_clientAuth;
                                case "emailProtection" -> KeyPurposeId.id_kp_emailProtection;
                                default -> null;
                            })
                            .filter(Objects::nonNull)
                            .toArray(KeyPurposeId[]::new);

                    builder.addExtension(
                            new ASN1ObjectIdentifier("2.5.29.37"),
                            false,
                            new ExtendedKeyUsage(purposes)
                    );
                } catch (CertIOException e) {
                    throw new RuntimeException(e);
                }
            },

            // Subject Alternative Name
            "2.5.29.17", (builder, value) -> {
                String strValue = (String) value;
                GeneralName[] names = Arrays.stream(strValue.split(","))
                        .map(String::trim)
                        .map(n -> {
                            if (n.startsWith("DNS:")) return new GeneralName(GeneralName.dNSName, n.substring(4));
                            if (n.startsWith("email:")) return new GeneralName(GeneralName.rfc822Name, n.substring(6));
                            return new GeneralName(GeneralName.otherName, n);
                        })
                        .toArray(GeneralName[]::new);

                GeneralNames subjectAltNames = new GeneralNames(names);
                try {
                    builder.addExtension(
                            new ASN1ObjectIdentifier("2.5.29.17"),
                            false,
                            subjectAltNames
                    );
                } catch (CertIOException e) {
                    throw new RuntimeException(e);
                }
            },

            // CRL Distribution Points
            "2.5.29.31", (builder, value) -> {
                try {
                    String strValue = (String) value;
                    DistributionPoint[] points = Arrays.stream(strValue.split(","))
                            .map(String::trim)
                            .map(uri -> {
                                GeneralName gn = new GeneralName(GeneralName.uniformResourceIdentifier, uri);
                                GeneralNames gns = new GeneralNames(gn);
                                return new DistributionPoint(new DistributionPointName(gns), null, null);
                            })
                            .toArray(DistributionPoint[]::new);

                    builder.addExtension(
                            new ASN1ObjectIdentifier("2.5.29.31"),
                            false,
                            new CRLDistPoint(points)
                    );
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            },

            // Authority Information Access
            "1.3.6.1.5.5.7.1.1", (builder, value) -> {
                try {
                    String strValue = (String) value;
                    List<AccessDescription> accessList = new ArrayList<>();
                    for (String entry : strValue.split(",")) {
                        String[] parts = entry.split(":", 2);
                        if (parts.length == 2) {
                            if (parts[0].equalsIgnoreCase("ocsp")) {
                                accessList.add(new AccessDescription(
                                        AccessDescription.id_ad_ocsp,
                                        new GeneralName(GeneralName.uniformResourceIdentifier, parts[1])
                                ));
                            } else if (parts[0].equalsIgnoreCase("caIssuers")) {
                                accessList.add(new AccessDescription(
                                        AccessDescription.id_ad_caIssuers,
                                        new GeneralName(GeneralName.uniformResourceIdentifier, parts[1])
                                ));
                            }
                        }
                    }

                    AuthorityInformationAccess aia = new AuthorityInformationAccess(
                            accessList.toArray(new AccessDescription[0])
                    );

                    builder.addExtension(
                            new ASN1ObjectIdentifier("1.3.6.1.5.5.7.1.1"),
                            false,
                            aia
                    );
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            },
            // Certificate Policies
            "2.5.29.32", (builder, value) -> {
                try {
                    String strValue = (String) value;
                    PolicyInformation[] policies = Arrays.stream(strValue.split(","))
                            .map(String::trim)
                            .map(oid -> new PolicyInformation(new ASN1ObjectIdentifier(oid)))
                            .toArray(PolicyInformation[]::new);

                    builder.addExtension(
                            new ASN1ObjectIdentifier("2.5.29.32"),
                            false,
                            new CertificatePolicies(policies)
                    );
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            },

            // Subject Key Identifier
            "2.5.29.14", (builder, value) -> {
                try {
                    PublicKey key = (PublicKey) value;
                    SubjectKeyIdentifier ski = new JcaX509ExtensionUtils().createSubjectKeyIdentifier(key);
                    builder.addExtension(
                            new ASN1ObjectIdentifier("2.5.29.14"),
                            false,
                            ski
                    );
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            },

            // Authority Key Identifier
            "2.5.29.35", (builder, value) -> {
                try {
                    PublicKey key = (PublicKey) value;
                    AuthorityKeyIdentifier aki = new JcaX509ExtensionUtils().createAuthorityKeyIdentifier(key);
                    builder.addExtension(
                            new ASN1ObjectIdentifier("2.5.29.35"),
                            false,
                            aki
                    );
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
    );


    public static void addExtensions(X509v3CertificateBuilder builder, Map<String, String> extensions, PublicKey issuerPublicKey, PublicKey subjectPublicKey) {
        if (extensions == null) return;

        for (Map.Entry<String, String> entry : extensions.entrySet()) {
            String name = entry.getKey().toLowerCase();
            String value = entry.getValue();

            BiConsumer<X509v3CertificateBuilder, Object> handler = EXTENSION_HANDLERS.get(name);
            if (name.equals("2.5.29.14")) { // SKI
                EXTENSION_HANDLERS.get(name).accept(builder, subjectPublicKey);
            } else if (name.equals("2.5.29.35")) { // AKI
                EXTENSION_HANDLERS.get(name).accept(builder, issuerPublicKey);
            } else {
                handler = EXTENSION_HANDLERS.get(name);
                if (handler != null) {
                    handler.accept(builder, value);
                } else {
                    System.out.println("Unknown extension: " + name);
                }
            }
        }
    }


    public static X500Name getSubjectX500Name(X509Certificate certificate) {
        String subjectDN = certificate.getSubjectX500Principal().getName();
        return new X500Name(subjectDN);
    }

    public static String getRDNValue(X500Name x500Name, ASN1ObjectIdentifier id) {
        RDN[] rdns = x500Name.getRDNs(id);
        if (rdns != null && rdns.length > 0 && rdns[0].getFirst() != null) {
            return rdns[0].getFirst().getValue().toString();
        }
        return null;
    }

    public static boolean isValidByDate(X509Certificate certificate) {
        try {
            certificate.checkValidity();
            return true;
        } catch (CertificateException e) {
            return false;
        }
    }

    public static boolean isValidSignature(X509Certificate certificate, X509Certificate issuerCertificate) {
        try {
            certificate.verify(issuerCertificate.getPublicKey());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Map<String, String> extractExtensionsFromAttributes(Attribute[] attributes) {
        Map<String, String> extensionMap = new HashMap<>();

        if (attributes == null || attributes.length == 0) {
            return extensionMap;
        }

        try {
            ASN1Encodable attrValue = attributes[0].getAttrValues().getObjectAt(0);
            Extensions extensions = Extensions.getInstance(attrValue);

            for (ASN1ObjectIdentifier oid : extensions.getExtensionOIDs()) {
                Extension ext = extensions.getExtension(oid);

                String name = oid.getId();
                String value;
                try {
                    value = ext.getParsedValue().toString();
                } catch (Exception e) {
                    value = Base64.getEncoder().encodeToString(ext.getExtnValue().getOctets());
                }

                extensionMap.put(name, value);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to extract extensions from CSR attributes", e);
        }

        return extensionMap;
    }

    public static byte[] pemToDer(String pem) {
        String base64 = pem.replaceAll("-----BEGIN (.*)-----", "")
                .replaceAll("-----END (.*)-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(base64);
    }

}

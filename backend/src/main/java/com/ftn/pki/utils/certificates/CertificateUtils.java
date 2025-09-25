package com.ftn.pki.utils.certificates;

import com.ftn.pki.models.certificates.CertificateType;
import com.ftn.pki.models.certificates.Issuer;
import com.ftn.pki.models.certificates.Subject;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
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

            // Always add CRL Distribution Point
            DistributionPoint[] points = new DistributionPoint[] {
                    new DistributionPoint(
                            new DistributionPointName(
                                    new GeneralNames(
                                            new GeneralName(GeneralName.uniformResourceIdentifier,
                                                    "https://localhost:8081/api/crl/latest")
                                    )
                            ),
                            null,
                            null
                    )
            };

            certGen.addExtension(
                    Extension.cRLDistributionPoints,
                    false,
                    new CRLDistPoint(points)
            );

            // the optional extensions
            addExtensions(certGen, extensions);

            X509CertificateHolder certHolder = certGen.build(contentSigner);

            JcaX509CertificateConverter certConverter = new JcaX509CertificateConverter();
            certConverter = certConverter.setProvider("BC");

            return certConverter.getCertificate(certHolder);

        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (OperatorCreationException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (CertIOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static final Map<String, BiConsumer<X509v3CertificateBuilder, String>> EXTENSION_HANDLERS =
            Map.of(
                    "keyusage", (builder, value) -> {
                        // value: "digitalSignature,keyEncipherment,dataEncipherment"
                        int usage = 0;
                        if (value.contains("digitalSignature")) usage |= KeyUsage.digitalSignature;
                        if (value.contains("keyEncipherment")) usage |= KeyUsage.keyEncipherment;
                        if (value.contains("dataEncipherment")) usage |= KeyUsage.dataEncipherment;
                        if (value.contains("keyCertSign")) usage |= KeyUsage.keyCertSign;
                        if (value.contains("cRLSign")) usage |= KeyUsage.cRLSign;

                        try {
                            builder.addExtension(
                                    Extension.keyUsage,
                                    true,
                                    new KeyUsage(usage)
                            );
                        } catch (CertIOException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    "extendedkeyusage", (builder, value) -> {
                        // value: "serverAuth,clientAuth,emailProtection"
                        try {
                            KeyPurposeId[] purposes = Arrays.stream(value.split(","))
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
                                    Extension.extendedKeyUsage,
                                    false,
                                    new ExtendedKeyUsage(purposes)
                            );
                        } catch (CertIOException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    "subjectaltname", (builder, value) -> {
                        // value: "email@example.com" ili "DNS:example.com"
                        GeneralName[] names = Arrays.stream(value.split(","))
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
                                    Extension.subjectAlternativeName,
                                    false,
                                    subjectAltNames
                            );
                        } catch (CertIOException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    "keycertsign", (builder, value) -> {
                        // value: "true" or "false"
                        try {
                            int usage = KeyUsage.keyCertSign;
                            builder.addExtension(
                                    Extension.keyUsage,
                                    true,
                                    new KeyUsage(usage)
                            );
                        } catch (CertIOException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    "digitalsignature", (builder, value) -> {
                        // value: "true" or "false"
                        try {
                            int usage = KeyUsage.digitalSignature;
                            builder.addExtension(
                                    Extension.keyUsage,
                                    true,
                                    new KeyUsage(usage)
                            );
                        } catch (CertIOException e) {
                            throw new RuntimeException(e);
                        }
                    },
                    "crldistributionpoints", (builder, value) -> {
                        // value: "http://example.com/crl1,http://example.com/crl2"
                        try {
                            DistributionPoint[] points = Arrays.stream(value.split(","))
                                    .map(String::trim)
                                    .map(uri -> {
                                        GeneralName gn = new GeneralName(GeneralName.uniformResourceIdentifier, uri);
                                        GeneralNames gns = new GeneralNames(gn);
                                        return new DistributionPoint(new DistributionPointName(gns), null, null);
                                    })
                                    .toArray(DistributionPoint[]::new);

                            builder.addExtension(
                                    Extension.cRLDistributionPoints,
                                    false,
                                    new CRLDistPoint(points)
                            );
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    },
                    "authorityinfoaccess", (builder, value) -> {
                        // value: "ocsp:http://ocsp.example.com,caIssuers:http://ca.example.com/ca.crt"
                        try {
                            List<AccessDescription> accessList = new ArrayList<>();
                            for (String entry : value.split(",")) {
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
                                    Extension.authorityInfoAccess,
                                    false,
                                    aia
                            );
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

            );

    public static void addExtensions(X509v3CertificateBuilder builder, Map<String, String> extensions) {
        if (extensions == null) return;

        for (Map.Entry<String, String> entry : extensions.entrySet()) {
            String name = entry.getKey().toLowerCase();
            String value = entry.getValue();

            BiConsumer<X509v3CertificateBuilder, String> handler = EXTENSION_HANDLERS.get(name);
            if (handler != null) {
                handler.accept(builder, value);
            } else {
                System.out.println("Unknown extension: " + name);
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
}

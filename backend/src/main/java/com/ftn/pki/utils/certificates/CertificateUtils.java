package com.ftn.pki.utils.certificates;

import com.ftn.pki.models.certificates.CertificateType;
import com.ftn.pki.models.certificates.Issuer;
import com.ftn.pki.models.certificates.Subject;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
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
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
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
                                                      String issuerCertificateId,
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
            String crlUrl = "https://localhost:8081/api/crl/" + issuerCertificateId + "/latest";

            DistributionPoint[] points = new DistributionPoint[] {
                    new DistributionPoint(
                            new DistributionPointName(
                                    new GeneralNames(
                                            new GeneralName(GeneralName.uniformResourceIdentifier, crlUrl)
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
                String strValue = ((String) value).toLowerCase().trim();
                int usage = 0;
                if (strValue.contains("digitalsignature")) usage |= KeyUsage.digitalSignature;
                if (strValue.contains("nonrepudiation")) usage |= KeyUsage.nonRepudiation;
                if (strValue.contains("keyencipherment")) usage |= KeyUsage.keyEncipherment;
                if (strValue.contains("dataencipherment")) usage |= KeyUsage.dataEncipherment;
                if (strValue.contains("keyagreement")) usage |= KeyUsage.keyAgreement;
                if (strValue.contains("keycertsign")) usage |= KeyUsage.keyCertSign;
                if (strValue.contains("crlsign")) usage |= KeyUsage.cRLSign;
                if (strValue.contains("encipheronly")) usage |= KeyUsage.encipherOnly;
                if (strValue.contains("decipheronly")) usage |= KeyUsage.decipherOnly;

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
                                case "serverAuth"     -> KeyPurposeId.id_kp_serverAuth;
                                case "clientAuth"     -> KeyPurposeId.id_kp_clientAuth;
                                case "codeSigning"    -> KeyPurposeId.id_kp_codeSigning;
                                case "emailProtection"-> KeyPurposeId.id_kp_emailProtection;
                                case "timeStamping"   -> KeyPurposeId.id_kp_timeStamping;
                                case "OCSPSigning"    -> KeyPurposeId.id_kp_OCSPSigning;
                                default               -> null;
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
                        .map(CertificateUtils::parseSANEntry)
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
            }
    );

    private static GeneralName parseSANEntry(String input) {
        String trimmed = input.trim();

        // Format "DNS:example.com"
        if (trimmed.contains(":") && !trimmed.matches(".*\\d+\\s*=.*")) {
            String[] parts = trimmed.split(":", 2);
            String type = parts[0].toUpperCase();
            String value = parts[1];
            return switch (type) {
                case "DNS"    -> new GeneralName(GeneralName.dNSName, value);
                case "IP"     -> new GeneralName(GeneralName.iPAddress, value);
                case "EMAIL"  -> new GeneralName(GeneralName.rfc822Name, value);
                case "URI"    -> new GeneralName(GeneralName.uniformResourceIdentifier, value);
                case "DIRNAME"-> new GeneralName(GeneralName.directoryName, value);
                case "OTHERNAME" -> new GeneralName(GeneralName.otherName, value);
                default -> throw new IllegalArgumentException("Unknown SAN type: " + type);
            };
        }

        // Format "DNS.1 = example.com"
        if (trimmed.contains("=")) {
            String[] parts = trimmed.split("=", 2);
            String left = parts[0].trim().toUpperCase();
            String value = parts[1].trim();

            if (left.startsWith("DNS")) return new GeneralName(GeneralName.dNSName, value);
            if (left.startsWith("IP")) return new GeneralName(GeneralName.iPAddress, value);
            if (left.startsWith("EMAIL")) return new GeneralName(GeneralName.rfc822Name, value);
            if (left.startsWith("URI")) return new GeneralName(GeneralName.uniformResourceIdentifier, value);
            if (left.startsWith("DIRNAME")) return new GeneralName(GeneralName.directoryName, value);
            if (left.startsWith("OTHERNAME")) return new GeneralName(GeneralName.otherName, value);
        }

        throw new IllegalArgumentException("Unsupported SAN format: " + input);
    }



    public static void addExtensions(X509v3CertificateBuilder builder, Map<String, String> extensions, PublicKey issuerPublicKey, PublicKey subjectPublicKey) {
        if (extensions == null) return;

        for (Map.Entry<String, String> entry : extensions.entrySet()) {
            String name = entry.getKey().toLowerCase();
            String value = entry.getValue();

            BiConsumer<X509v3CertificateBuilder, Object> handler = EXTENSION_HANDLERS.get(name);

            handler = EXTENSION_HANDLERS.get(name);
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
        } catch (CertificateExpiredException | CertificateNotYetValidException e) {
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

    public static Map<String, String> extractExtensionsFromCSR(PKCS10CertificationRequest csr) {
        Map<String, String> extensionMap = new HashMap<>();
        if (csr == null) return extensionMap;

        try {
            Attribute[] attributes = csr.getAttributes(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest);
            if (attributes == null || attributes.length == 0) return extensionMap;

            ASN1Encodable attrValue = attributes[0].getAttrValues().getObjectAt(0);
            Extensions extensions = Extensions.getInstance(attrValue);

            for (ASN1ObjectIdentifier oid : extensions.getExtensionOIDs()) {
                Extension ext = extensions.getExtension(oid);
                String name = ext.getExtnId().getId();
                String value;

                try {
                    switch (name) {
                        case "2.5.29.15": // Key Usage
                            KeyUsage ku = KeyUsage.getInstance(ext.getParsedValue());
                            List<String> usages = new ArrayList<>();
                            if ((ku.getBytes()[0] & KeyUsage.digitalSignature) != 0) usages.add("digitalSignature");
                            if ((ku.getBytes()[0] & KeyUsage.nonRepudiation) != 0) usages.add("nonRepudiation");
                            if ((ku.getBytes()[0] & KeyUsage.keyEncipherment) != 0) usages.add("keyEncipherment");
                            if ((ku.getBytes()[0] & KeyUsage.dataEncipherment) != 0) usages.add("dataEncipherment");
                            if ((ku.getBytes()[0] & KeyUsage.keyAgreement) != 0) usages.add("keyAgreement");
                            if ((ku.getBytes()[0] & KeyUsage.keyCertSign) != 0) usages.add("keyCertSign");
                            if ((ku.getBytes()[0] & KeyUsage.cRLSign) != 0) usages.add("cRLSign");
                            if ((ku.getBytes()[0] & KeyUsage.encipherOnly) != 0) usages.add("encipherOnly");
                            if ((ku.getBytes()[0] & KeyUsage.decipherOnly) != 0) usages.add("decipherOnly");
                            value = String.join(",", usages);
                            break;

                        case "2.5.29.37": // Extended Key Usage
                            ExtendedKeyUsage eku = ExtendedKeyUsage.getInstance(ext.getParsedValue());
                            List<String> purposes = new ArrayList<>();
                            for (KeyPurposeId kp : eku.getUsages()) {
                                if (KeyPurposeId.id_kp_serverAuth.equals(kp)) purposes.add("serverAuth");
                                else if (KeyPurposeId.id_kp_clientAuth.equals(kp)) purposes.add("clientAuth");
                                else if (KeyPurposeId.id_kp_codeSigning.equals(kp)) purposes.add("codeSigning");
                                else if (KeyPurposeId.id_kp_emailProtection.equals(kp)) purposes.add("emailProtection");
                                else if (KeyPurposeId.id_kp_timeStamping.equals(kp)) purposes.add("timeStamping");
                                else if (KeyPurposeId.id_kp_OCSPSigning.equals(kp)) purposes.add("OCSPSigning");
                            }
                            value = String.join(",", purposes);
                            break;

                        case "2.5.29.17": // Subject Alternative Name
                            GeneralNames gns = GeneralNames.getInstance(ext.getParsedValue());
                            List<String> names = new ArrayList<>();
                            for (GeneralName gn : gns.getNames()) {
                                switch (gn.getTagNo()) {
                                    case GeneralName.dNSName -> names.add("DNS:" + gn.getName().toString());
                                    case GeneralName.iPAddress -> names.add("IP:" + gn.getName().toString());
                                    case GeneralName.rfc822Name -> names.add("EMAIL:" + gn.getName().toString());
                                    case GeneralName.uniformResourceIdentifier -> names.add("URI:" + gn.getName().toString());
                                    case GeneralName.directoryName -> names.add("DIRNAME:" + gn.getName().toString());
                                    case GeneralName.otherName -> names.add("OTHERNAME:" + gn.getName().toString());
                                    default -> names.add(gn.getName().toString());
                                }
                            }
                            value = String.join(",", names);
                            break;

                        default: // fallback
                            try {
                                value = ext.getParsedValue().toString();
                            } catch (Exception e) {
                                value = Base64.getEncoder().encodeToString(ext.getExtnValue().getOctets());
                            }
                    }

                    extensionMap.put(name, value);

                } catch (Exception e) {
                    System.out.println("Failed to parse extension " + name + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to extract extensions from CSR", e);
        }

        return extensionMap;
    }

    public static byte[] pemToDer(String pem) {
        String base64 = pem.replaceAll("-----BEGIN (.*)-----", "")
                .replaceAll("-----END (.*)-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(base64);
    }

    public static String derToPem(byte[] derBytes) {
        String base64 = Base64.getMimeEncoder(64, "\n".getBytes())
                .encodeToString(derBytes);
        return "-----BEGIN CERTIFICATE-----\n" +
                base64 +
                "\n-----END CERTIFICATE-----\n";
    }


}

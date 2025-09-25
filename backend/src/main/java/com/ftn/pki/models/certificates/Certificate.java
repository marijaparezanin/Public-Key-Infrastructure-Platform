package com.ftn.pki.models.certificates;

import com.ftn.pki.models.organizations.Organization;
import com.ftn.pki.models.users.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CERTIFICATES")
public class Certificate {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CertificateType type;

    @Column(nullable = false)
    private String serialNumber;

    @Column(nullable = false)
    private Date startDate;
    @Column(nullable = false)
    private Date endDate;

    @Lob
    private byte[] certificateEncoded; // X509Certificate.getEncoded()

    @Lob
    private byte[] privateKeyEncrypted; // private key encrypted with DEK

    @Column
    private String iv; // IV for AES encryption of private key with DEK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issuer_id", nullable = true)
    private Certificate issuer;

    @Column(nullable = false)
    private boolean revoked = false;

    public X509Certificate getX509Certificate() throws Exception {
        return (X509Certificate) java.security.cert.CertificateFactory
                .getInstance("X.509")
                .generateCertificate(new java.io.ByteArrayInputStream(this.certificateEncoded));
    }
}

package com.ftn.pki.models.certificates;

import com.ftn.pki.models.organizations.Organization;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CertificateTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issuer_id", nullable = false)
    private Certificate issuer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    /**
     * Common Name (CN) – regularni izraz za validaciju
     */
    @Column(nullable = false)
    private String commonNameRegex;

    /**
     * Subject Alternative Names (SANs) – regularni izraz
     */
    @Column
    private String subjectAlternativeNameRegex;

    /**
     * TTL (vreme važenja) – maksimalno trajanje u danima
     */
    @Column(nullable = false)
    private Integer ttlDays;

    /**
     * Key Usage – podrazumevana vrednost
     */
    @Column
    private String keyUsage; // could be CSV or JSON string, depending on how you store it

    /**
     * Extended Key Usage – podrazumevana vrednost
     */
    @Column
    private String extendedKeyUsage; // same here: CSV or JSON string
}

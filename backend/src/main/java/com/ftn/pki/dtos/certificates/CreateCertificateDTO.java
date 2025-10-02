package com.ftn.pki.dtos.certificates;

import com.ftn.pki.models.certificates.CertificateType;
import lombok.*;

import java.util.Date;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateCertificateDTO {
    private CertificateType type;

    // X500Name (subject)
    private String commonName;        // CN
    private String surname;           // SURNAME
    private String givenName;         // GIVENNAME
    private String organization;      // O
    private String organizationalUnit;// OU
    private String country;           // C
    private String email;             // E

    private Date startDate;
    private Date endDate;

    private Map<String, String> extensions;

    private String assignToOrganizationName;

    private String issuerCertificateId;
}

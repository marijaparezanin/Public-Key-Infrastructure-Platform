package com.ftn.pki.dtos.certificates;

import com.ftn.pki.models.certificates.CertificateType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreatedCertificateDTO {
    private UUID id;
    private CertificateType type;
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
}

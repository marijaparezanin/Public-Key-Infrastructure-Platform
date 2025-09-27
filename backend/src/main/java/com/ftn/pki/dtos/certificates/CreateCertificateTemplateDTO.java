package com.ftn.pki.dtos.certificates;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateCertificateTemplateDTO {
    private String name;
    private UUID issuerCertificateId;
    private String commonNameRegex;
    private String subjectAlternativeNameRegex;
    private int ttlDays;
    private String keyUsage;
    private String extendedKeyUsage;
}

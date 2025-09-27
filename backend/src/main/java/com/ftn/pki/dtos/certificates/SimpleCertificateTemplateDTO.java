package com.ftn.pki.dtos.certificates;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SimpleCertificateTemplateDTO {
    private String name;
    private String commonNameRegex;
    private String subjectAlternativeNameRegex;
    private int ttlDays;
    private String keyUsage;
    private String extendedKeyUsage;
}

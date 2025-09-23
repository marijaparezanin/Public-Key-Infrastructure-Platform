package com.ftn.pki.dtos.certificates;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SimpleCertificateDTO {
    UUID id;
    String serialNumber;
    String subjectCommonName;
    Date validTo;
}

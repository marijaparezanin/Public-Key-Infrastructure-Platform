package com.ftn.pki.dtos.certificates;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadCsrDTO {
    private String issuerCertificateId;
    private Date validFrom;
    private Date validTo;
}

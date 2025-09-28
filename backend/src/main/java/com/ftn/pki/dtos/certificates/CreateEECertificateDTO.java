package com.ftn.pki.dtos.certificates;

import com.ftn.pki.models.certificates.KEYSTOREDOWNLOADFORMAT;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateEECertificateDTO extends CreateCertificateDTO {
    private String alias;
    private String password;
    private KEYSTOREDOWNLOADFORMAT keyStoreFormat;
}

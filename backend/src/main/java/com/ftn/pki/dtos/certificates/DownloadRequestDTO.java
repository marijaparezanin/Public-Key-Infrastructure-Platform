package com.ftn.pki.dtos.certificates;

import com.ftn.pki.models.certificates.KEYSTOREDOWNLOADFORMAT;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DownloadRequestDTO {
    private UUID certificateId;
    private KEYSTOREDOWNLOADFORMAT format;
    private String password;
    private String alias;
}

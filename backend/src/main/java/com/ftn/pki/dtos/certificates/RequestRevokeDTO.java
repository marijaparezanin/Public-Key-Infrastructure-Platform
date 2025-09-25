package com.ftn.pki.dtos.certificates;

import com.ftn.pki.models.certificates.RevocationReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RequestRevokeDTO {
    private String certificateId;
    private RevocationReason reason;
}

package com.ftn.pki.dtos.certificates;

import com.ftn.pki.models.certificates.RevocationReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RequestRevokeDTO {
    private UUID certificateId;
    private RevocationReason reason;
}

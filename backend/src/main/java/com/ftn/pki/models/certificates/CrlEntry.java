package com.ftn.pki.models.certificates;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class CrlEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String certificateSerialNumber;

    private UUID issuerId;

    @Temporal(TemporalType.TIMESTAMP)
    private Date revocationDate;

    @Enumerated(EnumType.STRING)
    private RevocationReason reason;
}
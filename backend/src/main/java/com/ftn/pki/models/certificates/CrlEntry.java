package com.ftn.pki.models.certificates;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class CrlEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    private String certificateSerialNumber;

    @Temporal(TemporalType.TIMESTAMP)
    private Date revocationDate;

    @Enumerated(EnumType.STRING)
    private RevocationReason reason;
}
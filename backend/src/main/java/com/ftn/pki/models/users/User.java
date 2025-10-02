package com.ftn.pki.models.users;

import com.ftn.pki.models.organizations.Organization;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "USERS")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String keycloakId;

    @Column(unique = true)
    private String email;

    @Column
    private String firstname;

    @Column
    private String lastname;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id", nullable = true)
    private Organization organization;

    @Column
    @Enumerated(EnumType.STRING)
    private UserRole role;
}

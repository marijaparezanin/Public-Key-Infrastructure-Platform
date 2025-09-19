package com.ftn.pki.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "USER_")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
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

    @Column
    private String organization;

}

package com.ftn.pki.dtos.users;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateCAUserDTO {
    private String email;
    private String name;
    private String surname;
    private String organization;
}

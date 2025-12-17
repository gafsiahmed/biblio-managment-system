package com.bibliotheque.dto;

import com.bibliotheque.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserForm {
    private Long id;

    @NotNull
    @Size(min = 3, max = 100)
    private String username;

    @NotNull
    @Email
    private String email;

    @Size(min = 8, max = 255)
    private String password; // Optional on update

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    private String phone;
    private String address;

    @NotNull
    private Role role;

    private Long libraryId;
}

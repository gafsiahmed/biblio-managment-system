package com.bibliotheque.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordDto {
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String currentPassword;

    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String newPassword;

    private String confirmPassword;
}

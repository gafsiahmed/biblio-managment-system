package com.bibliotheque.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileDto {
  @Size(max = 100)
  private String firstName;

  @Size(max = 100)
  private String lastName;

  @Pattern(regexp = "^[+0-9][0-9\\-\\s]{7,20}$")
  private String phone;

  @Size(max = 255)
  private String address;

  private java.time.LocalDate dateOfBirth;
}


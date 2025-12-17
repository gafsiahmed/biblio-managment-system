package com.bibliotheque.model;

import com.bibliotheque.model.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Size(min = 3, max = 100)
  @Column(name = "username", nullable = false, unique = true, length = 100)
  private String username;

  @NotNull
  @Email
  @Column(name = "email", nullable = false, unique = true, length = 150)
  private String email;

  @NotNull
  @Size(min = 8, max = 255)
  @Column(name = "password", nullable = false, length = 255)
  private String password;

  @Size(max = 100)
  @Column(name = "first_name", length = 100)
  private String firstName;

  @Size(max = 100)
  @Column(name = "last_name", length = 100)
  private String lastName;

  @Pattern(regexp = "^[+0-9][0-9\\-\\s]{7,20}$")
  @Column(name = "phone", length = 25)
  private String phone;

  @Size(max = 255)
  @Column(name = "address", length = 255)
  private String address;

  @Column(name = "date_of_birth")
  private LocalDate dateOfBirth;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private ZonedDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private ZonedDateTime updatedAt;

  @Column(name = "enabled", nullable = false)
  private boolean enabled = true;

  @Column(name = "email_verified", nullable = false)
  private boolean emailVerified = false;

  @Size(max = 255)
  @Column(name = "verification_token", length = 255)
  private String verificationToken;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 50)
  private Role role = Role.ROLE_USER;

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
  private List<Loan> loans = new ArrayList<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "library_id")
  private Library library;

  @PrePersist
  private void prePersist() {
    if (createdAt == null) {
      createdAt = ZonedDateTime.now();
    }
    if (role == null) {
      role = Role.ROLE_USER;
    }
  }

  public Integer getAge() {
    if (dateOfBirth == null) {
      return null;
    }
    return Period.between(dateOfBirth, LocalDate.now()).getYears();
  }

  public boolean isAccountNonExpired() {
    return true;
  }

  public boolean isAccountNonLocked() {
    return enabled;
  }
}


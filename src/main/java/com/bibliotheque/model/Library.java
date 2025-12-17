package com.bibliotheque.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "libraries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Library {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Size(min = 2, max = 150)
  @Column(name = "name", nullable = false, length = 150)
  private String name;

  @Size(max = 255)
  @Column(name = "address", length = 255)
  private String address;

  @Size(max = 120)
  @Column(name = "city", length = 120)
  private String city;

  @Size(max = 15)
  @Column(name = "postal_code", length = 15)
  private String postalCode;

  @Size(max = 25)
  @Column(name = "phone", length = 25)
  private String phone;

  @Email
  @Size(max = 150)
  @Column(name = "email", length = 150)
  private String email;

  @Column(name = "latitude")
  private Double latitude;

  @Column(name = "longitude")
  private Double longitude;

  @Size(max = 255)
  @Column(name = "opening_hours", length = 255)
  private String openingHours;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private ZonedDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private ZonedDateTime updatedAt;

  @OneToMany(mappedBy = "library", fetch = FetchType.LAZY)
  private List<Resource> resources = new ArrayList<>();

  @OneToMany(mappedBy = "library", fetch = FetchType.LAZY)
  private List<User> staff = new ArrayList<>();

  @PrePersist
  private void prePersist() {
    if (createdAt == null) {
      createdAt = ZonedDateTime.now();
    }
  }
}


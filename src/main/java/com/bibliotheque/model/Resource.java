package com.bibliotheque.model;

import com.bibliotheque.model.enums.Category;
import jakarta.persistence.*;
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
@Table(name = "resources")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "resource_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Resource {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull
  @Size(min = 1, max = 255)
  @Column(name = "title", nullable = false, length = 255)
  private String title;

  @Size(max = 255)
  @Column(name = "author", length = 255)
  private String author;

  @Size(max = 50)
  @Column(name = "isbn", length = 50)
  private String isbn;

  @Size(max = 1000)
  @Column(name = "description", length = 1000)
  private String description;

  @Column(name = "publication_year")
  private Integer publicationYear;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "category", nullable = false, length = 50)
  private Category category;

  @Size(max = 255)
  @Column(name = "publisher", length = 255)
  private String publisher;

  @Size(max = 100)
  @Column(name = "language", length = 100)
  private String language;

  @Column(name = "total_copies")
  private Integer totalCopies = 0;

  @Column(name = "available_copies")
  private Integer availableCopies = 0;

  @Column(name = "reservation_count")
  private Integer reservationCount = 0;

  @Column(name = "rating")
  private Double rating;

  @Size(max = 255)
  @Column(name = "cover_image", length = 255)
  private String coverImage;

  @Column(name = "is_active", nullable = false)
  private boolean isActive = true;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private ZonedDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private ZonedDateTime updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "library_id")
  private Library library;

  @OneToMany(mappedBy = "resource", fetch = FetchType.LAZY)
  private List<Loan> loans = new ArrayList<>();
}

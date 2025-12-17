package com.bibliotheque.dto;

import com.bibliotheque.model.enums.Category;
import com.bibliotheque.model.enums.ResourceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResourceForm {

  private Long id;

  @NotNull(message = "Le type de ressource est obligatoire")
  private ResourceType type;

  @NotNull(message = "Le titre est obligatoire")
  @Size(min = 1, max = 255)
  private String title;

  @Size(max = 255)
  private String author;

  @Size(max = 50)
  private String isbn;

  @Size(max = 1000)
  private String description;

  private Integer publicationYear;

  @NotNull(message = "La catégorie est obligatoire")
  private Category category;

  @Size(max = 255)
  private String publisher;

  @Size(max = 100)
  private String language;

  private Integer totalCopies = 0;
  private Integer availableCopies = 0;

  // Book specific
  @Size(max = 100)
  private String edition;

  @Size(max = 50)
  private String volume;

  @Size(max = 255)
  private String series;

  @Size(max = 50)
  private String isbn13;

  @Size(max = 50)
  private String isbn10;

  // Digital specific
  @Size(max = 255)
  private String fileUrl;

  @Size(max = 50)
  private String fileFormat;

  private Long fileSize;
  private boolean streamingAvailable;

  @NotNull(message = "La bibliothèque est obligatoire")
  private Long libraryId;
}

package com.bibliotheque.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("BOOK")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Book extends Resource {

  @Column(name = "edition", length = 100)
  private String edition;

  @Column(name = "volume", length = 50)
  private String volume;

  @Column(name = "series", length = 255)
  private String series;

  @Column(name = "isbn13", length = 50)
  private String isbn13;

  @Column(name = "isbn10", length = 50)
  private String isbn10;
}


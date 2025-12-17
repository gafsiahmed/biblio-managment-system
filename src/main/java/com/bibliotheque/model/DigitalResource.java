package com.bibliotheque.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("DIGITAL")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DigitalResource extends Resource {

  @Column(name = "file_url", length = 255)
  private String fileUrl;

  @Column(name = "file_format", length = 50)
  private String fileFormat;

  @Column(name = "file_size")
  private Long fileSize;

  @Column(name = "download_count")
  private Integer downloadCount = 0;

  @Column(name = "streaming_available")
  private boolean streamingAvailable = false;
}


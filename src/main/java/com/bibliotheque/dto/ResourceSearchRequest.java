package com.bibliotheque.dto;

import com.bibliotheque.model.enums.Category;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class ResourceSearchRequest {
    private String query;
    private String author;
    private Category category;
    private Boolean available;
    private Integer yearMin;
    private Integer yearMax;
    private Long libraryId;
    private String sort = "title"; // title, author, date, popularity
    private String direction = "asc";
    private int page = 0;
    private int size = 12;
}

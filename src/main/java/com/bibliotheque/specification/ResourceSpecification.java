package com.bibliotheque.specification;

import com.bibliotheque.dto.ResourceSearchRequest;
import com.bibliotheque.model.Loan;
import com.bibliotheque.model.Resource;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ResourceSpecification {

    public static Specification<Resource> getSpecification(ResourceSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Active resources only
            predicates.add(criteriaBuilder.isTrue(root.get("isActive")));

            // Text Search (Title or ISBN)
            if (request.getQuery() != null && !request.getQuery().trim().isEmpty()) {
                String likePattern = "%" + request.getQuery().toLowerCase() + "%";
                Predicate titleLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), likePattern);
                Predicate isbnLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("isbn")), likePattern);
                predicates.add(criteriaBuilder.or(titleLike, isbnLike));
            }

            // Author
            if (request.getAuthor() != null && !request.getAuthor().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("author")), "%" + request.getAuthor().toLowerCase() + "%"));
            }

            // Category
            if (request.getCategory() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), request.getCategory()));
            }

            // Availability
            if (request.getAvailable() != null && request.getAvailable()) {
                predicates.add(criteriaBuilder.greaterThan(root.get("availableCopies"), 0));
            }

            // Publication Year Range
            if (request.getYearMin() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("publicationYear"), request.getYearMin()));
            }
            if (request.getYearMax() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("publicationYear"), request.getYearMax()));
            }

            // Library
            if (request.getLibraryId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("library").get("id"), request.getLibraryId()));
            }
            
            // Apply predicates
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

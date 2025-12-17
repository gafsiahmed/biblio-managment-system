package com.bibliotheque.service;

import com.bibliotheque.model.Resource;
import com.bibliotheque.model.enums.Category;
import com.bibliotheque.repository.ResourceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ResourceService {

  private final ResourceRepository resourceRepository;

  public ResourceService(ResourceRepository resourceRepository) {
    this.resourceRepository = resourceRepository;
  }

  public Page<Resource> list(Pageable pageable) {
    return resourceRepository.findAll(pageable);
  }

  public Optional<Resource> find(Long id) {
    return resourceRepository.findById(id);
  }

  public Resource save(Resource resource) {
    if (resource.getTotalCopies() == null) resource.setTotalCopies(0);
    if (resource.getAvailableCopies() == null) resource.setAvailableCopies(0);
    if (resource.getAvailableCopies() > resource.getTotalCopies()) {
      resource.setAvailableCopies(resource.getTotalCopies());
    }
    return resourceRepository.save(resource);
  }

  public void delete(Long id) {
    resourceRepository.deleteById(id);
  }

  public Page<Resource> search(String query, String author, Category category, Boolean available, Long libraryId, Pageable pageable) {
    Specification<Resource> spec = (root, q, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (query != null && !query.isBlank()) {
        String like = "%" + query.toLowerCase() + "%";
        predicates.add(cb.like(cb.lower(root.get("title")), like));
      }
      if (author != null && !author.isBlank()) {
        String like = "%" + author.toLowerCase() + "%";
        predicates.add(cb.like(cb.lower(root.get("author")), like));
      }
      if (category != null) {
        predicates.add(cb.equal(root.get("category"), category));
      }
      if (libraryId != null) {
        predicates.add(cb.equal(root.get("library").get("id"), libraryId));
      }
      if (available != null) {
        if (available) {
          predicates.add(cb.greaterThan(root.get("availableCopies"), 0));
        } else {
          predicates.add(cb.lessThanOrEqualTo(root.get("availableCopies"), 0));
        }
      }
      return cb.and(predicates.toArray(new Predicate[0]));
    };
    return resourceRepository.findAll(spec, pageable);
  }

  public Resource updateAvailability(Resource resource, int delta) {
    int total = Optional.ofNullable(resource.getTotalCopies()).orElse(0);
    int available = Optional.ofNullable(resource.getAvailableCopies()).orElse(0);
    int newAvailable = Math.max(0, Math.min(total, available + delta));
    resource.setAvailableCopies(newAvailable);
    return resourceRepository.save(resource);
  }
}


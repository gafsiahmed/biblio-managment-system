package com.bibliotheque.repository;

import com.bibliotheque.model.Resource;
import com.bibliotheque.model.enums.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ResourceRepository extends JpaRepository<Resource, Long>, JpaSpecificationExecutor<Resource> {
  Page<Resource> findByCategory(Category category, Pageable pageable);
}

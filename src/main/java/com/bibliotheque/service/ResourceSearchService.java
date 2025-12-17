package com.bibliotheque.service;

import com.bibliotheque.dto.ResourceSearchRequest;
import com.bibliotheque.model.Resource;
import com.bibliotheque.repository.ResourceRepository;
import com.bibliotheque.specification.ResourceSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceSearchService {

    private final ResourceRepository resourceRepository;

    public Page<Resource> search(ResourceSearchRequest request) {
        Specification<Resource> spec = ResourceSpecification.getSpecification(request);
        Pageable pageable = createPageable(request);
        return resourceRepository.findAll(spec, pageable);
    }

    private Pageable createPageable(ResourceSearchRequest request) {
        Sort sort = Sort.unsorted();
        
        String sortField = request.getSort();
        String direction = request.getDirection();
        
        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;

        if ("title".equalsIgnoreCase(sortField)) {
            sort = Sort.by(dir, "title");
        } else if ("author".equalsIgnoreCase(sortField)) {
            sort = Sort.by(dir, "author");
        } else if ("date".equalsIgnoreCase(sortField)) {
            sort = Sort.by(dir, "publicationYear");
        } else if ("popularity".equalsIgnoreCase(sortField)) {
            sort = Sort.by(dir, "reservationCount"); 
        } else {
            sort = Sort.by(dir, "title");
        }

        return PageRequest.of(request.getPage(), request.getSize(), sort);
    }
}

package com.bibliotheque.service;

import com.bibliotheque.model.Book;
import com.bibliotheque.model.Resource;
import com.bibliotheque.repository.ResourceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @InjectMocks
    private ResourceService resourceService;

    @Test
    void testListResources() {
        Resource resource = new Book();
        resource.setTitle("Test Resource");
        Page<Resource> page = new PageImpl<>(Collections.singletonList(resource));

        when(resourceRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<Resource> result = resourceService.list(Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Resource", result.getContent().get(0).getTitle());
        verify(resourceRepository).findAll(any(Pageable.class));
    }
}

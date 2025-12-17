package com.bibliotheque.controller;

import com.bibliotheque.dto.ResourceForm;
import com.bibliotheque.model.enums.Category;
import com.bibliotheque.model.enums.ResourceType;
import com.bibliotheque.service.FileStorageService;
import com.bibliotheque.service.ResourceService;
import com.bibliotheque.repository.ResourceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ResourceController.class)
public class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResourceService resourceService;

    @MockBean
    private com.bibliotheque.service.ResourceSearchService resourceSearchService;

    @MockBean
    private com.bibliotheque.service.UserService userService;

    @MockBean
    private ResourceRepository resourceRepository;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private com.bibliotheque.repository.LibraryRepository libraryRepository;

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testCreateResource() throws Exception {
        org.mockito.Mockito.when(libraryRepository.findAll()).thenReturn(java.util.Collections.emptyList());
        org.mockito.Mockito.when(libraryRepository.findById(1L)).thenReturn(java.util.Optional.of(new com.bibliotheque.model.Library()));
        org.mockito.Mockito.when(libraryRepository.save(org.mockito.ArgumentMatchers.any())).thenReturn(new com.bibliotheque.model.Library());
        org.mockito.Mockito.when(userService.getCurrentUser()).thenReturn(java.util.Optional.empty());

        mockMvc.perform(post("/resources")
                .with(csrf())
                .param("type", "BOOK")
                .param("title", "Test Book")
                .param("category", "BOOK")
                .param("totalCopies", "10")
                .param("availableCopies", "10")
                .param("author", "Author")
                .param("isbn", "123456")
                .param("libraryId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/resources"));
    }

    @Test
    @WithMockUser(username = "user")
    public void testListResources() throws Exception {
        org.mockito.Mockito.when(resourceSearchService.search(org.mockito.ArgumentMatchers.any())).thenReturn(org.springframework.data.domain.Page.empty());
        org.mockito.Mockito.when(libraryRepository.findAll()).thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/resources"))
                .andExpect(status().isOk())
                .andExpect(view().name("resources/search"));
    }
}

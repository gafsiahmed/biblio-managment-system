package com.bibliotheque.controller;

import com.bibliotheque.model.Book;
import com.bibliotheque.model.Loan;
import com.bibliotheque.model.Resource;
import com.bibliotheque.model.User;
import com.bibliotheque.model.enums.LoanStatus;
import com.bibliotheque.service.LoanService;
import com.bibliotheque.service.ResourceService;
import com.bibliotheque.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class LoanControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanService loanService;

    @MockBean
    private UserService userService;

    @MockBean
    private ResourceService resourceService;

    @Test
    @WithMockUser(username = "user")
    void testMyLoans() throws Exception {
        User user = new User();
        user.setUsername("user");
        when(userService.getCurrentUser()).thenReturn(Optional.of(user));
        when(loanService.findByUser(user)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/loans/my-loans"))
                .andExpect(status().isOk())
                .andExpect(view().name("loans/my-loans"))
                .andExpect(model().attributeExists("loans"));
    }

    @Test
    @WithMockUser(username = "user")
    void testReserve_Success() throws Exception {
        User user = new User();
        Resource resource = new Book();
        resource.setId(1L);
        resource.setTitle("Test Book");
        resource.setAvailableCopies(1);

        when(userService.getCurrentUser()).thenReturn(Optional.of(user));
        when(resourceService.find(1L)).thenReturn(Optional.of(resource)); // Assuming find exists as discussed
        // If find() is missing in service, this mock might need adjustment depending on actual Service method
        // But LoanController calls resourceService.find(resourceId), so it must exist.

        mockMvc.perform(post("/loans/reserve/1")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/loans/my-loans"))
                .andExpect(flash().attributeExists("success"));
    }
}

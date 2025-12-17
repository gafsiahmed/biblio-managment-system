package com.bibliotheque.controller;

import com.bibliotheque.dto.UserForm;
import com.bibliotheque.model.User;
import com.bibliotheque.model.enums.Role;
import com.bibliotheque.repository.LibraryRepository;
import com.bibliotheque.repository.UserRepository;
import com.bibliotheque.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Utilisateurs", description = "Gestion administrative des utilisateurs")
public class UserController {

    private final UserRepository userRepository;
    private final LibraryRepository libraryRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Operation(summary = "Liste des utilisateurs", description = "Affiche la liste de tous les utilisateurs")
    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "users/list";
    }

    @Operation(summary = "Formulaire nouvel utilisateur", description = "Affiche le formulaire de création d'utilisateur")
    @GetMapping("/new")
    public String createForm(Model model) {
        UserForm form = new UserForm();
        form.setRole(Role.ROLE_USER);
        model.addAttribute("userForm", form);
        model.addAttribute("roles", Role.values());
        model.addAttribute("libraries", libraryRepository.findAll());
        return "users/form";
    }

    @Operation(summary = "Créer un utilisateur", description = "Enregistre un nouvel utilisateur")
    @PostMapping
    public String create(@Valid @ModelAttribute("userForm") UserForm form, BindingResult result, Model model) {
        if (form.getPassword() == null || form.getPassword().isEmpty()) {
            result.rejectValue("password", "error.userForm", "Le mot de passe est obligatoire");
        }

        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            model.addAttribute("libraries", libraryRepository.findAll());
            return "users/form";
        }

        if (userRepository.existsByEmail(form.getEmail())) {
             result.rejectValue("email", "error.userForm", "Cet email est déjà utilisé");
             model.addAttribute("roles", Role.values());
             model.addAttribute("libraries", libraryRepository.findAll());
             return "users/form";
        }
        
        if (userRepository.findByUsername(form.getUsername()).isPresent()) {
             result.rejectValue("username", "error.userForm", "Ce nom d'utilisateur est déjà utilisé");
             model.addAttribute("roles", Role.values());
             model.addAttribute("libraries", libraryRepository.findAll());
             return "users/form";
        }

        User user = new User();
        mapFormToUser(form, user);
        
        // Specifics for new user
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setEmailVerified(false);
        user.setPassword(passwordEncoder.encode(form.getPassword()));

        userRepository.save(user);

        // Send Email
        emailService.sendAccountCreationEmail(
            user.getEmail(),
            user.getFirstName() + " " + user.getLastName(),
            user.getUsername(),
            form.getPassword(),
            token
        );

        return "redirect:/users";
    }

    @Operation(summary = "Formulaire édition utilisateur", description = "Affiche le formulaire d'édition d'utilisateur")
    @GetMapping("/{id}/edit")
    public String editForm(@Parameter(description = "ID de l'utilisateur") @PathVariable Long id, Model model) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        
        UserForm form = new UserForm();
        form.setId(user.getId());
        form.setUsername(user.getUsername());
        form.setEmail(user.getEmail());
        form.setFirstName(user.getFirstName());
        form.setLastName(user.getLastName());
        form.setPhone(user.getPhone());
        form.setAddress(user.getAddress());
        form.setRole(user.getRole());
        if (user.getLibrary() != null) {
            form.setLibraryId(user.getLibrary().getId());
        }

        model.addAttribute("userForm", form);
        model.addAttribute("roles", Role.values());
        model.addAttribute("libraries", libraryRepository.findAll());
        return "users/form";
    }

    @Operation(summary = "Mettre à jour un utilisateur", description = "Enregistre les modifications d'un utilisateur")
    @PostMapping("/{id}")
    public String update(@Parameter(description = "ID de l'utilisateur") @PathVariable Long id, @Valid @ModelAttribute("userForm") UserForm form, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            model.addAttribute("libraries", libraryRepository.findAll());
            return "users/form";
        }

        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));

        if (!user.getEmail().equals(form.getEmail()) && userRepository.existsByEmail(form.getEmail())) {
             result.rejectValue("email", "error.userForm", "Cet email est déjà utilisé");
             model.addAttribute("roles", Role.values());
             model.addAttribute("libraries", libraryRepository.findAll());
             return "users/form";
        }
        if (!user.getUsername().equals(form.getUsername()) && userRepository.findByUsername(form.getUsername()).isPresent()) {
             result.rejectValue("username", "error.userForm", "Ce nom d'utilisateur est déjà utilisé");
             model.addAttribute("roles", Role.values());
             model.addAttribute("libraries", libraryRepository.findAll());
             return "users/form";
        }

        mapFormToUser(form, user);
        
        // Password update only if provided
        if (form.getPassword() != null && !form.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(form.getPassword()));
        }

        userRepository.save(user);
        return "redirect:/users";
    }

    private void mapFormToUser(UserForm form, User user) {
        user.setUsername(form.getUsername());
        user.setEmail(form.getEmail());
        user.setFirstName(form.getFirstName());
        user.setLastName(form.getLastName());
        user.setPhone(form.getPhone());
        user.setAddress(form.getAddress());
        user.setRole(form.getRole());
        
        if (form.getRole() == Role.ROLE_LIBRARIAN && form.getLibraryId() != null) {
            libraryRepository.findById(form.getLibraryId()).ifPresent(user::setLibrary);
        } else {
            user.setLibrary(null);
        }
    }
}

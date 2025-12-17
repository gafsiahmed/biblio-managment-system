package com.bibliotheque.controller;

import com.bibliotheque.model.Library;
import com.bibliotheque.repository.LibraryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/libraries")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Bibliothèques", description = "Gestion des bibliothèques physiques")
public class LibraryController {

    private final LibraryRepository libraryRepository;

    public LibraryController(LibraryRepository libraryRepository) {
        this.libraryRepository = libraryRepository;
    }

    @Operation(summary = "Liste des bibliothèques", description = "Affiche la liste de toutes les bibliothèques")
    @GetMapping
    public String list(Model model) {
        model.addAttribute("libraries", libraryRepository.findAll());
        return "libraries/list";
    }

    @Operation(summary = "Formulaire nouvelle bibliothèque", description = "Affiche le formulaire de création")
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("library", new Library());
        return "libraries/form";
    }

    @Operation(summary = "Créer une bibliothèque", description = "Enregistre une nouvelle bibliothèque")
    @PostMapping
    public String create(@Valid @ModelAttribute("library") Library library, BindingResult result) {
        if (result.hasErrors()) {
            return "libraries/form";
        }
        libraryRepository.save(library);
        return "redirect:/libraries";
    }

    @Operation(summary = "Formulaire édition bibliothèque", description = "Affiche le formulaire d'édition")
    @GetMapping("/{id}/edit")
    public String editForm(@Parameter(description = "ID de la bibliothèque") @PathVariable Long id, Model model) {
        Library library = libraryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid library Id:" + id));
        model.addAttribute("library", library);
        return "libraries/form";
    }

    @Operation(summary = "Mettre à jour une bibliothèque", description = "Enregistre les modifications d'une bibliothèque")
    @PostMapping("/{id}")
    public String update(@Parameter(description = "ID de la bibliothèque") @PathVariable Long id, @Valid @ModelAttribute("library") Library library, BindingResult result) {
        if (result.hasErrors()) {
            return "libraries/form";
        }
        library.setId(id);
        libraryRepository.save(library);
        return "redirect:/libraries";
    }

    @Operation(summary = "Supprimer une bibliothèque", description = "Supprime une bibliothèque existante")
    @GetMapping("/{id}/delete")
    public String delete(@Parameter(description = "ID de la bibliothèque") @PathVariable Long id) {
        libraryRepository.deleteById(id);
        return "redirect:/libraries";
    }
}

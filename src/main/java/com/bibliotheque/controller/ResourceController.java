package com.bibliotheque.controller;

import com.bibliotheque.dto.ResourceForm;
import com.bibliotheque.model.Book;
import com.bibliotheque.model.DigitalResource;
import com.bibliotheque.model.Resource;
import com.bibliotheque.model.User;
import com.bibliotheque.model.enums.Role;
import com.bibliotheque.service.UserService;
import com.bibliotheque.model.enums.Category;
import com.bibliotheque.model.enums.ResourceType;
import com.bibliotheque.model.Library;
import com.bibliotheque.repository.LibraryRepository;
import com.bibliotheque.repository.ResourceRepository;
import com.bibliotheque.service.ResourceService;
import com.bibliotheque.service.FileStorageService;
import com.bibliotheque.service.ResourceSearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.AccessDeniedException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@Tag(name = "Ressources", description = "Catalogue des livres et ressources")
public class ResourceController {

  private final ResourceService resourceService;
  private final ResourceSearchService resourceSearchService;
  private final ResourceRepository resourceRepository;
  private final FileStorageService fileStorageService;
  private final LibraryRepository libraryRepository;
  private final UserService userService;

  public ResourceController(ResourceService resourceService, ResourceSearchService resourceSearchService, ResourceRepository resourceRepository, FileStorageService fileStorageService, LibraryRepository libraryRepository, UserService userService) {
    this.resourceService = resourceService;
    this.resourceSearchService = resourceSearchService;
    this.resourceRepository = resourceRepository;
    this.fileStorageService = fileStorageService;
    this.libraryRepository = libraryRepository;
    this.userService = userService;
  }

  @Operation(summary = "Mes ressources (Bibliothécaire)", description = "Recherche dans les ressources de la bibliothèque du bibliothécaire")
  @GetMapping("/librarian/my-resources")
  public String myResources(@ModelAttribute com.bibliotheque.dto.ResourceSearchRequest request, Model model) {
      Optional<User> currentUser = userService.getCurrentUser();
      if (currentUser.isPresent() && currentUser.get().getRole() == Role.ROLE_LIBRARIAN && currentUser.get().getLibrary() != null) {
          request.setLibraryId(currentUser.get().getLibrary().getId());
          
          Page<Resource> resources = resourceSearchService.search(request);
          model.addAttribute("resources", resources);
          model.addAttribute("request", request);
          model.addAttribute("categories", Category.values());
          model.addAttribute("libraries", libraryRepository.findAll());
          model.addAttribute("isLibrarianView", true);
          return "resources/search";
      }
      return "redirect:/resources";
  }

  @Operation(summary = "Liste des ressources", description = "Affiche la page de recherche principale des ressources")
  @GetMapping("/resources")
  public String list(@ModelAttribute com.bibliotheque.dto.ResourceSearchRequest request, Model model) {
    Page<Resource> resources = resourceSearchService.search(request);
    model.addAttribute("resources", resources);
    model.addAttribute("request", request);
    model.addAttribute("categories", Category.values());
    model.addAttribute("libraries", libraryRepository.findAll());
    return "resources/search";
  }

  @Operation(summary = "Détail ressource", description = "Affiche les détails d'une ressource")
  @GetMapping("/resources/{id}")
  public String detail(@Parameter(description = "ID de la ressource") @PathVariable Long id, Model model) {
    Optional<Resource> r = resourceService.find(id);
    if (r.isEmpty()) {
      return "redirect:/resources";
    }
    Resource resource = r.get();
    model.addAttribute("resource", resource);
    
    boolean canEdit = false;
    Optional<User> currentUser = userService.getCurrentUser();
    if (currentUser.isPresent()) {
      User user = currentUser.get();
      if (user.getRole() == Role.ROLE_ADMIN) {
        canEdit = true;
      } else if (user.getRole() == Role.ROLE_LIBRARIAN && user.getLibrary() != null && resource.getLibrary() != null) {
        canEdit = user.getLibrary().getId().equals(resource.getLibrary().getId());
      }
    }
    model.addAttribute("canEdit", canEdit);
    
    return "resources/detail";
  }

  @Operation(summary = "Recherche simple", description = "Recherche des ressources avec filtres basiques")
  @GetMapping("/resources/search")
  public String search(@RequestParam(value = "query", required = false) String query,
                       @RequestParam(value = "author", required = false) String author,
                       @RequestParam(value = "category", required = false) Category category,
                       @RequestParam(value = "available", required = false) Boolean available,
                       @RequestParam(value = "libraryId", required = false) Long libraryId,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "10") int size,
                       Model model) {
    Page<Resource> resources = resourceService.search(query, author, category, available, libraryId, PageRequest.of(page, size));
    model.addAttribute("resources", resources);
    model.addAttribute("query", query);
    model.addAttribute("author", author);
    model.addAttribute("selectedCategory", category);
    model.addAttribute("selectedLibrary", libraryId);
    model.addAttribute("libraries", libraryRepository.findAll());
    return "resources/list";
  }

  @Operation(summary = "Créer une ressource", description = "Enregistre une nouvelle ressource (Livre ou Numérique)")
  @PostMapping("/resources")
  public String create(@Valid @ModelAttribute("resource") ResourceForm form, BindingResult result, Model model) {
    if (result.hasErrors()) {
      model.addAttribute("categories", Category.values());
      model.addAttribute("types", ResourceType.values());
      model.addAttribute("libraries", libraryRepository.findAll());
      return "resources/form";
    }

    Resource resource;
    if (form.getType() == ResourceType.BOOK) {
      Book book = new Book();
      book.setEdition(form.getEdition());
      book.setVolume(form.getVolume());
      book.setSeries(form.getSeries());
      book.setIsbn13(form.getIsbn13());
      book.setIsbn10(form.getIsbn10());
      resource = book;
    } else {
      DigitalResource digital = new DigitalResource();
      digital.setFileUrl(form.getFileUrl());
      digital.setFileFormat(form.getFileFormat());
      digital.setFileSize(form.getFileSize());
      digital.setStreamingAvailable(form.isStreamingAvailable());
      resource = digital;
    }

    resource.setTitle(form.getTitle());
    resource.setAuthor(form.getAuthor());
    resource.setIsbn(form.getIsbn());
    resource.setDescription(form.getDescription());
    resource.setPublicationYear(form.getPublicationYear());
    resource.setCategory(form.getCategory());
    resource.setPublisher(form.getPublisher());
    resource.setLanguage(form.getLanguage());
    resource.setTotalCopies(form.getTotalCopies());
    resource.setAvailableCopies(form.getAvailableCopies());

    Optional<User> currentUser = userService.getCurrentUser();
    if (currentUser.isPresent() && currentUser.get().getRole() == Role.ROLE_LIBRARIAN) {
      if (currentUser.get().getLibrary() != null) {
        resource.setLibrary(currentUser.get().getLibrary());
      } else {
        if (form.getLibraryId() != null) {
          libraryRepository.findById(form.getLibraryId()).ifPresent(resource::setLibrary);
        }
      }
    } else {
      if (form.getLibraryId() != null) {
        libraryRepository.findById(form.getLibraryId()).ifPresent(resource::setLibrary);
      } else {
         List<Library> libraries = libraryRepository.findAll();
         if (!libraries.isEmpty()) {
             resource.setLibrary(libraries.get(0));
         }
      }
    }

    resourceService.save(resource);
    return "redirect:/resources";
  }

  @Operation(summary = "Formulaire nouvelle ressource", description = "Affiche le formulaire de création de ressource")
  @GetMapping("/resources/new")
  public String formNew(Model model) {
    ResourceForm form = new ResourceForm();
    form.setType(ResourceType.BOOK); // Default
    model.addAttribute("resource", form);
    model.addAttribute("categories", Category.values());
    model.addAttribute("types", ResourceType.values());
    model.addAttribute("libraries", libraryRepository.findAll());
    return "resources/form";
  }

  @Operation(summary = "Formulaire édition ressource", description = "Affiche le formulaire d'édition de ressource")
  @GetMapping("/resources/{id}/edit")
  public String formEdit(@Parameter(description = "ID de la ressource") @PathVariable Long id, Model model) {
    Optional<Resource> r = resourceService.find(id);
    if (r.isEmpty()) {
      return "redirect:/resources";
    }
    Resource resource = r.get();
    ResourceForm form = new ResourceForm();
    form.setId(resource.getId());
    form.setTitle(resource.getTitle());
    form.setAuthor(resource.getAuthor());
    form.setCategory(resource.getCategory());
    form.setIsbn(resource.getIsbn());
    form.setPublicationYear(resource.getPublicationYear());
    form.setPublisher(resource.getPublisher());
    form.setLanguage(resource.getLanguage());
    form.setDescription(resource.getDescription());
    form.setTotalCopies(resource.getTotalCopies());
    form.setAvailableCopies(resource.getAvailableCopies());
    if (resource.getLibrary() != null) {
      form.setLibraryId(resource.getLibrary().getId());
    }

    if (resource instanceof Book) {
      Book b = (Book) resource;
      form.setType(ResourceType.BOOK);
      form.setEdition(b.getEdition());
      form.setVolume(b.getVolume());
      form.setSeries(b.getSeries());
      form.setIsbn10(b.getIsbn10());
      form.setIsbn13(b.getIsbn13());
    } else if (resource instanceof DigitalResource) {
      DigitalResource d = (DigitalResource) resource;
      form.setType(ResourceType.DIGITAL);
      form.setFileUrl(d.getFileUrl());
      form.setFileFormat(d.getFileFormat());
      form.setFileSize(d.getFileSize());
      form.setStreamingAvailable(d.isStreamingAvailable());
    }

    model.addAttribute("resource", form);
    model.addAttribute("categories", Category.values());
    model.addAttribute("types", ResourceType.values());
    model.addAttribute("libraries", libraryRepository.findAll());
    return "resources/form";
  }

  @Operation(summary = "Mettre à jour une ressource", description = "Enregistre les modifications d'une ressource")
  @PostMapping("/resources/{id}")
  public String update(@Parameter(description = "ID de la ressource") @PathVariable Long id, @Valid @ModelAttribute("resource") ResourceForm form, BindingResult result, Model model) {
    if (result.hasErrors()) {
      model.addAttribute("categories", Category.values());
      model.addAttribute("types", ResourceType.values());
      model.addAttribute("libraries", libraryRepository.findAll());
      return "resources/form";
    }

    Optional<Resource> r = resourceService.find(id);
    if (r.isEmpty()) {
      return "redirect:/resources";
    }
    
    Resource resource = r.get();
    
    // Common fields
    resource.setTitle(form.getTitle());
    resource.setAuthor(form.getAuthor());
    resource.setIsbn(form.getIsbn());
    resource.setDescription(form.getDescription());
    resource.setPublicationYear(form.getPublicationYear());
    resource.setCategory(form.getCategory());
    resource.setPublisher(form.getPublisher());
    resource.setLanguage(form.getLanguage());
    resource.setTotalCopies(form.getTotalCopies());
    resource.setAvailableCopies(form.getAvailableCopies());
    
    // Assign library and Security Check
    Optional<User> currentUser = userService.getCurrentUser();
    if (currentUser.isPresent() && currentUser.get().getRole() == Role.ROLE_LIBRARIAN) {
       // Validate that we are updating a resource that belongs to us
       if (resource.getLibrary() != null && currentUser.get().getLibrary() != null) {
           if (!resource.getLibrary().getId().equals(currentUser.get().getLibrary().getId())) {
               throw new AccessDeniedException("You cannot update a resource that belongs to another library.");
           }
       }
       
       // Force library to be the librarian's library
       if (currentUser.get().getLibrary() != null) {
         resource.setLibrary(currentUser.get().getLibrary());
       }
    } else if (form.getLibraryId() != null) {
      libraryRepository.findById(form.getLibraryId()).ifPresent(resource::setLibrary);
    }

    // Update specific fields
    if (resource instanceof Book && form.getType() == ResourceType.BOOK) {
      Book b = (Book) resource;
      b.setEdition(form.getEdition());
      b.setVolume(form.getVolume());
      b.setSeries(form.getSeries());
      b.setIsbn10(form.getIsbn10());
      b.setIsbn13(form.getIsbn13());
    } else if (resource instanceof DigitalResource && form.getType() == ResourceType.DIGITAL) {
      DigitalResource d = (DigitalResource) resource;
      d.setFileUrl(form.getFileUrl());
      d.setFileFormat(form.getFileFormat());
      d.setFileSize(form.getFileSize());
      d.setStreamingAvailable(form.isStreamingAvailable());
    }
    

    resourceService.save(resource);
    return "redirect:/resources/" + id;
  }


  @Operation(summary = "Supprimer une ressource", description = "Supprime une ressource existante")
  @DeleteMapping("/resources/{id}")
  public ResponseEntity<Void> delete(@Parameter(description = "ID de la ressource") @PathVariable Long id) {
    Optional<Resource> r = resourceService.find(id);
    if (r.isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    Resource resource = r.get();
    
    // Security check
    Optional<User> currentUser = userService.getCurrentUser();
    if (currentUser.isPresent() && currentUser.get().getRole() == Role.ROLE_LIBRARIAN) {
        if (resource.getLibrary() == null || currentUser.get().getLibrary() == null || 
            !resource.getLibrary().getId().equals(currentUser.get().getLibrary().getId())) {
            return ResponseEntity.status(403).build();
        }
    }

    resourceService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Uploader une couverture", description = "Télécharge une image de couverture pour la ressource")
  @PostMapping("/resources/{id}/upload-cover")
  public String uploadCover(@Parameter(description = "ID de la ressource") @PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
    Optional<Resource> r = resourceService.find(id);
    if (r.isEmpty()) {
      return "redirect:/resources";
    }
    Resource resource = r.get();

    Optional<User> currentUser = userService.getCurrentUser();
    if (currentUser.isPresent() && currentUser.get().getRole() == Role.ROLE_LIBRARIAN) {
        if (resource.getLibrary() == null || currentUser.get().getLibrary() == null || 
            !resource.getLibrary().getId().equals(currentUser.get().getLibrary().getId())) {
            throw new AccessDeniedException("You can only edit resources from your own library.");
        }
    }

    String path = fileStorageService.storeCover(file);
    resource.setCoverImage(path);
    resourceService.save(resource);
    return "redirect:/resources/" + id;
  }
}


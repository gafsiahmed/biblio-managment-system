package com.bibliotheque.controller;

import com.bibliotheque.dto.ResourceSearchRequest;
import com.bibliotheque.model.Resource;
import com.bibliotheque.model.enums.Category;
import com.bibliotheque.service.ResourceSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/resources")
@RequiredArgsConstructor
@Tag(name = "Recherche avancée", description = "Moteur de recherche multicritères")
public class ResourceSearchController {

    private final ResourceSearchService resourceSearchService;

    @Operation(summary = "Recherche avancée", description = "Effectue une recherche détaillée sur les ressources")
    @GetMapping("/search-advanced")
    public String search(@ModelAttribute ResourceSearchRequest request, Model model) {
        Page<Resource> results = resourceSearchService.search(request);
        
        model.addAttribute("resources", results);
        model.addAttribute("request", request);
        model.addAttribute("categories", Category.values());
        
        return "resources/search";
    }
}

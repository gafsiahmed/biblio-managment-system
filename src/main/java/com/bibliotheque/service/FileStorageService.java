package com.bibliotheque.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {
  private final Path root = Paths.get("uploads", "covers");

  public String storeCover(MultipartFile file) throws IOException {
    if (!Files.exists(root)) {
      Files.createDirectories(root);
    }
    String ext = extractExtension(file.getOriginalFilename());
    String name = UUID.randomUUID() + (ext != null ? ("." + ext) : "");
    Path target = root.resolve(name);
    Files.copy(file.getInputStream(), target);
    return target.toString().replace("\\", "/");
  }

  private String extractExtension(String original) {
    if (original == null) return null;
    int idx = original.lastIndexOf('.');
    if (idx > -1 && idx < original.length() - 1) {
      return original.substring(idx + 1);
    }
    return null;
  }
}


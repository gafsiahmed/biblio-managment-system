package com.bibliotheque.model.enums;

public enum ResourceType {
  BOOK("Livre"),
  DIGITAL("Ressource Numérique");

  private final String label;

  ResourceType(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}

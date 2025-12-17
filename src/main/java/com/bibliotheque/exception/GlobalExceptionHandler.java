package com.bibliotheque.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(Exception.class)
  public String handleException(Exception e, Model model) {
    e.printStackTrace(); // Log to console
    model.addAttribute("error", e.getMessage());
    model.addAttribute("trace", e.getStackTrace());
    return "error";
  }
}

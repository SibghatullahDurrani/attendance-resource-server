package com.main.face_recognition_resource_server.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("csrf")
public class CSRFController {
  @GetMapping("/")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  public CsrfToken csrf(CsrfToken token) {
    return token;
  }
}

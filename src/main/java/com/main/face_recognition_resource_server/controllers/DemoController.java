package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.services.camera.CameraServices;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoController {
  private final CameraServices cameraServices;

  public DemoController(CameraServices cameraServices) {
    this.cameraServices = cameraServices;
  }

  @PreAuthorize("hasRole('SUPER_ADMIN')")
  @GetMapping("/start-subscription")
  public void demo() {
    cameraServices.startInFaceRecognitionSubscription("192.168.100.37", 80);
  }

}

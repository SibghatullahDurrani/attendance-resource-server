package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.DepartmentCameraDTO;
import com.main.face_recognition_resource_server.DTOS.RegisterCameraDTO;
import com.main.face_recognition_resource_server.services.camera.CameraServices;
import com.main.face_recognition_resource_server.services.user.UserServices;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("cameras")
public class CameraController {
  private final CameraServices cameraServices;
  private final UserServices userServices;

  public CameraController(CameraServices cameraServices, UserServices userServices) {
    this.cameraServices = cameraServices;
    this.userServices = userServices;
  }

  @GetMapping("department")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<List<DepartmentCameraDTO>> getCameraOfOwnDepartment(Authentication authentication) {
    Long departmentId = userServices.getUserDepartmentId(authentication.getName());
    List<DepartmentCameraDTO> departmentCameras = cameraServices.getCamerasOfDepartment(departmentId);
    return new ResponseEntity<>(departmentCameras, HttpStatus.OK);
  }

  @GetMapping("department/{id}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<List<DepartmentCameraDTO>> getCameraOfDepartment(@PathVariable Long id) {
    List<DepartmentCameraDTO> departmentCameras = cameraServices.getCamerasOfDepartment(id);
    return new ResponseEntity<>(departmentCameras, HttpStatus.OK);
  }

  @PostMapping()
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<HttpStatus> registerCamera(@RequestBody RegisterCameraDTO cameraToRegister) {
    //TODO: Implement this method
    return null;
  }
}

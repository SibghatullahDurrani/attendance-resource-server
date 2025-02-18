package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.GetCameraDTO;
import com.main.face_recognition_resource_server.DTOS.RegisterCameraDTO;
import com.main.face_recognition_resource_server.domains.Organization;
import com.main.face_recognition_resource_server.exceptions.CameraAlreadyExistsInOrganizationException;
import com.main.face_recognition_resource_server.exceptions.NoInCameraExistsException;
import com.main.face_recognition_resource_server.exceptions.OrganizationDoesntExistException;
import com.main.face_recognition_resource_server.services.camera.CameraServices;
import com.main.face_recognition_resource_server.services.camera.CameraSubscriptionServices;
import com.main.face_recognition_resource_server.services.organization.OrganizationServices;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("cameras")
public class CameraController {
  private final CameraServices cameraServices;
  private final OrganizationServices organizationServices;
  private final CameraSubscriptionServices cameraSubscriptionServices;

  public CameraController(CameraServices cameraServices, OrganizationServices organizationServices, CameraSubscriptionServices cameraSubscriptionServices) {
    this.cameraServices = cameraServices;
    this.organizationServices = organizationServices;
    this.cameraSubscriptionServices = cameraSubscriptionServices;
  }

  @PostMapping()
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<HttpStatus> registerCamera(@RequestBody RegisterCameraDTO cameraToRegister) throws
          CameraAlreadyExistsInOrganizationException, OrganizationDoesntExistException {
    Organization organization = organizationServices.getOrganization(cameraToRegister.getOrganizationId());
    cameraServices.registerCamera(cameraToRegister, organization);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping("organization/{organizationId}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<List<GetCameraDTO>> getCameraOfOrganization(@PathVariable Long organizationId) throws OrganizationDoesntExistException {
    boolean organizationExists = organizationServices.organizationExists(organizationId);
    if (organizationExists) {
      List<GetCameraDTO> organizationCameras = cameraServices.getCamerasOfOrganization(organizationId);
      return new ResponseEntity<>(organizationCameras, HttpStatus.OK);
    }
    return null;
  }

  @PostMapping("start-face-recognition/organization/{organizationId}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<HttpStatus> startFaceRecognitionOfOrganization(@PathVariable Long organizationId) throws NoInCameraExistsException {
    cameraSubscriptionServices.startFaceRecognitionSubscription(organizationId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PostMapping("stop-face-recognition/organization/{organizationId}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<HttpStatus> stopFaceRecognitionOfOrganization(@PathVariable Long organizationId) {
    cameraSubscriptionServices.stopFaceRecognitionSubscription(organizationId);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}

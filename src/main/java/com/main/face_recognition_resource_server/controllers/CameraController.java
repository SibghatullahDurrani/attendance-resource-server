package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.GetCameraDTO;
import com.main.face_recognition_resource_server.DTOS.RegisterCameraDTO;
import com.main.face_recognition_resource_server.constants.CameraMode;
import com.main.face_recognition_resource_server.domains.Department;
import com.main.face_recognition_resource_server.domains.Organization;
import com.main.face_recognition_resource_server.exceptions.CameraAlreadyExistsInOrganizationException;
import com.main.face_recognition_resource_server.services.camera.CameraServices;
import com.main.face_recognition_resource_server.services.department.DepartmentServices;
import com.main.face_recognition_resource_server.services.organization.OrganizationServices;
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
  private final DepartmentServices departmentServices;
  private final OrganizationServices organizationServices;

  public CameraController(CameraServices cameraServices, UserServices userServices, DepartmentServices departmentServices, OrganizationServices organizationServices) {
    this.cameraServices = cameraServices;
    this.userServices = userServices;
    this.departmentServices = departmentServices;
    this.organizationServices = organizationServices;
  }

  @GetMapping("department")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<List<GetCameraDTO>> getCameraOfOwnDepartment(Authentication authentication) {
    Long departmentId = userServices.getUserDepartmentId(authentication.getName());
    List<GetCameraDTO> departmentCameras = cameraServices.getCamerasOfDepartment(departmentId);
    return new ResponseEntity<>(departmentCameras, HttpStatus.OK);
  }

  @GetMapping("department/{id}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<List<GetCameraDTO>> getCameraOfDepartment(@PathVariable Long id) {
    boolean departmentExists = departmentServices.departmentExist(id);
    if (departmentExists) {
      List<GetCameraDTO> departmentCameras = cameraServices.getCamerasOfDepartment(id);
      return new ResponseEntity<>(departmentCameras, HttpStatus.OK);
    }
    return null;
  }

  @PostMapping()
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<HttpStatus> registerCamera(@RequestBody RegisterCameraDTO cameraToRegister) throws CameraAlreadyExistsInOrganizationException {
    if (cameraToRegister.getMode() == CameraMode.DEPARTMENTAL) {
      Department department = departmentServices.getDepartment(cameraToRegister.getId());
      cameraServices.registerCamera(cameraToRegister, department);
    } else {
      Organization organization = organizationServices.getOrganization(cameraToRegister.getId());
      cameraServices.registerCamera(cameraToRegister, organization);
    }
    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping("organization")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<List<GetCameraDTO>> getCameraOfOwnOrganization(Authentication authentication) {
    Long organizationId = userServices.getUserOrganizationId(authentication.getName());
    List<GetCameraDTO> organizationCameras = cameraServices.getCamerasOfOrganization(organizationId);
    return new ResponseEntity<>(organizationCameras, HttpStatus.OK);
  }

  @GetMapping("organization/{organizationId}")
  @PreAuthorize("hasRole('SUPER_ADMIN')")
  public ResponseEntity<List<GetCameraDTO>> getCameraOfOrganization(@PathVariable Long organizationId) {
    boolean organizationExists = organizationServices.organizationExists(organizationId);
    if (organizationExists) {
      List<GetCameraDTO> organizationCameras = cameraServices.getCamerasOfOrganization(organizationId);
      return new ResponseEntity<>(organizationCameras, HttpStatus.OK);
    }
    return null;
  }
}

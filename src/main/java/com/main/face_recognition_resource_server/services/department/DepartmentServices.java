package com.main.face_recognition_resource_server.services.department;

import com.main.face_recognition_resource_server.DTOS.DepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.RegisterDepartmentDTO;
import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntBelongToYourOrganizationException;
import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntExistException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public interface DepartmentServices {

  boolean departmentExist(Long departmentId) throws DepartmentDoesntExistException;

  boolean departmentBelongsToOrganization(Long departmentId, Long organizationId) throws DepartmentDoesntExistException, DepartmentDoesntBelongToYourOrganizationException;

  ResponseEntity<DepartmentDTO> getDepartmentByUsername(String username);

  ResponseEntity<HttpStatus> registerDepartmentAsSuperAdmin(RegisterDepartmentDTO departmentToRegister);

  ResponseEntity<HttpStatus> registerDepartmentAsAdmin(RegisterDepartmentDTO departmentToRegister, String name);
}

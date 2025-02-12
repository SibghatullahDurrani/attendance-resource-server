package com.main.face_recognition_resource_server.services.department;

import com.main.face_recognition_resource_server.DTOS.DepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.RegisterDepartmentDTO;
import com.main.face_recognition_resource_server.exceptions.OrganizationDoesntExistException;
import com.main.face_recognition_resource_server.repositories.DepartmentRepository;
import com.main.face_recognition_resource_server.repositories.OrganizationRepository;
import com.main.face_recognition_resource_server.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DepartmentServicesImpl implements DepartmentServices {
  private final DepartmentRepository departmentRepository;
  private final UserRepository userRepository;
  private final OrganizationRepository organizationRepository;


  public DepartmentServicesImpl(DepartmentRepository departmentRepository, UserRepository userRepository, OrganizationRepository organizationRepository) {
    this.departmentRepository = departmentRepository;
    this.userRepository = userRepository;
    this.organizationRepository = organizationRepository;
  }

  @Override
  public ResponseEntity<DepartmentDTO> getDepartmentByUsername(String username) {
    Optional<DepartmentDTO> optionalDepartment = userRepository.getDepartmentByUsername(username);
    return optionalDepartment.map(department -> new ResponseEntity<>(department, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(null, HttpStatus.NOT_FOUND));
  }


  @Override
  public ResponseEntity<HttpStatus> registerDepartmentAsSuperAdmin(RegisterDepartmentDTO departmentToRegister) {
    boolean organizationExists = organizationRepository.existsById(departmentToRegister.getOrganizationId());
    if (!organizationExists) {
      throw new OrganizationDoesntExistException();
    }
    departmentRepository.registerDepartment(departmentToRegister.getDepartmentName(), departmentToRegister.getOrganizationId());
    return new ResponseEntity<>(HttpStatus.CREATED);
  }
}

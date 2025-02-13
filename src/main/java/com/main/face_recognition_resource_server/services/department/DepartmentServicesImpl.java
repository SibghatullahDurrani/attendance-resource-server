package com.main.face_recognition_resource_server.services.department;

import com.main.face_recognition_resource_server.DTOS.RegisterDepartmentDTO;
import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntBelongToYourOrganizationException;
import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntExistException;
import com.main.face_recognition_resource_server.exceptions.OrganizationDoesntBelongToYouException;
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
  public boolean departmentExist(Long departmentId) throws DepartmentDoesntExistException {
    boolean exists = departmentRepository.existsById(departmentId);
    if (!exists) {
      throw new DepartmentDoesntExistException();
    } else {
      return true;
    }
  }

  @Override
  public boolean departmentBelongsToOrganization(Long departmentId, Long organizationId) throws DepartmentDoesntExistException, DepartmentDoesntBelongToYourOrganizationException {
    Optional<Long> organizationIdOfDepartment = departmentRepository.getOrganizationIdOfDepartment(departmentId);
    if (organizationIdOfDepartment.isEmpty()) {
      throw new DepartmentDoesntExistException();
    } else {
      if (!organizationIdOfDepartment.get().equals(organizationId)) {
        throw new DepartmentDoesntBelongToYourOrganizationException();
      }
    }
    return true;
  }

  @Override
  public void registerDepartment(RegisterDepartmentDTO departmentToRegister) {
    departmentRepository.registerDepartment(departmentToRegister.getDepartmentName(), departmentToRegister.getOrganizationId());
  }
}

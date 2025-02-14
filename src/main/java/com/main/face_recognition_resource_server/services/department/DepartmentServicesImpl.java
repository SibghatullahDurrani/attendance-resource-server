package com.main.face_recognition_resource_server.services.department;

import com.main.face_recognition_resource_server.DTOS.RegisterDepartmentDTO;
import com.main.face_recognition_resource_server.domains.Department;
import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntBelongToYourOrganizationException;
import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntExistException;
import com.main.face_recognition_resource_server.repositories.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DepartmentServicesImpl implements DepartmentServices {
  private final DepartmentRepository departmentRepository;

  public DepartmentServicesImpl(DepartmentRepository departmentRepository) {
    this.departmentRepository = departmentRepository;
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

  @Override
  public Department getDepartment(Long departmentId) throws DepartmentDoesntExistException {
    Optional<Department> department = departmentRepository.findById(departmentId);
    if (department.isEmpty()) {
      throw new DepartmentDoesntExistException();
    } else {
      return department.get();
    }
  }
}

package com.main.face_recognition_resource_server.services.department;

import com.main.face_recognition_resource_server.DTOS.department.RegisterDepartmentDTO;
import com.main.face_recognition_resource_server.domains.Department;
import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntBelongToYourOrganizationException;
import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntExistException;
import com.main.face_recognition_resource_server.repositories.DepartmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
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
  public boolean departmentBelongsToOrganization(Long departmentId, Long organizationId)
          throws DepartmentDoesntExistException,
          DepartmentDoesntBelongToYourOrganizationException {
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

  @Override
  public List<Long> getDepartmentIdsOfOrganization(Long organizationId) {
    return departmentRepository.getDepartmentIdsOfOrganization(organizationId);
  }

  @Override
  public String getDepartmentName(Long departmentId) throws DepartmentDoesntExistException {
    Optional<String> departmentName = departmentRepository.getDepartmentName(departmentId);
    if (departmentName.isEmpty()) {
      throw new DepartmentDoesntExistException();
    }
    return departmentName.get();
  }

  @Override
  public List<String> getDepartmentNamesOfOrganization(Long organizationId) {
    return this.departmentRepository.getDepartmentNamesOfOrganization(organizationId);
  }
}

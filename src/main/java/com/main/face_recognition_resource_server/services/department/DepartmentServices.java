package com.main.face_recognition_resource_server.services.department;

import com.main.face_recognition_resource_server.DTOS.department.DepartmentsTableRecordDTO;
import com.main.face_recognition_resource_server.DTOS.department.RegisterDepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.organization.DepartmentOfOrganizationDTO;
import com.main.face_recognition_resource_server.domains.Department;
import com.main.face_recognition_resource_server.exceptions.DepartmentAlreadyExistsException;
import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntBelongToYourOrganizationException;
import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntExistException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DepartmentServices {

  boolean departmentExist(Long departmentId) throws DepartmentDoesntExistException;

  void checkIfDepartmentBelongsToOrganization(Long departmentId, Long organizationId) throws DepartmentDoesntExistException, DepartmentDoesntBelongToYourOrganizationException;

  void registerDepartment(RegisterDepartmentDTO departmentToRegister);

  Department getDepartment(Long departmentId) throws DepartmentDoesntExistException;

  List<Long> getDepartmentIdsOfOrganization(Long organizationId);

  String getDepartmentName(Long departmentId) throws DepartmentDoesntExistException;

  List<DepartmentOfOrganizationDTO> getDepartmentNamesOfOrganization(Long organizationId);

  Page<DepartmentsTableRecordDTO> getDepartmentsTableData(Long organizationId, Pageable pageable);

  void registerDepartments(List<RegisterDepartmentDTO> departmentsToRegister, Long organizationId) throws DepartmentAlreadyExistsException;
}

package com.main.face_recognition_resource_server.services.department;

import com.main.face_recognition_resource_server.DTOS.department.RegisterDepartmentDTO;
import com.main.face_recognition_resource_server.domains.Department;
import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntBelongToYourOrganizationException;
import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntExistException;

public interface DepartmentServices {

  boolean departmentExist(Long departmentId) throws DepartmentDoesntExistException;

  boolean departmentBelongsToOrganization(Long departmentId, Long organizationId) throws DepartmentDoesntExistException, DepartmentDoesntBelongToYourOrganizationException;

  void registerDepartment(RegisterDepartmentDTO departmentToRegister);

  Department getDepartment(Long departmentId) throws DepartmentDoesntExistException;
}

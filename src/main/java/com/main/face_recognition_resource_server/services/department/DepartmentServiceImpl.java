package com.main.face_recognition_resource_server.services.department;

import com.main.face_recognition_resource_server.DTOS.department.DepartmentsTableRecordDTO;
import com.main.face_recognition_resource_server.DTOS.department.RegisterDepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.organization.DepartmentOfOrganizationDTO;
import com.main.face_recognition_resource_server.domains.Department;
import com.main.face_recognition_resource_server.domains.Organization;
import com.main.face_recognition_resource_server.exceptions.DepartmentAlreadyExistsException;
import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntBelongToYourOrganizationException;
import com.main.face_recognition_resource_server.exceptions.DepartmentDoesntExistException;
import com.main.face_recognition_resource_server.repositories.DepartmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
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
    public void checkIfDepartmentBelongsToOrganization(Long departmentId, Long organizationId)
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
    }

    @Override
    public void registerDepartment(RegisterDepartmentDTO departmentToRegister) {
//    departmentRepository.registerDepartment(departmentToRegister.getDepartmentName(), departmentToRegister.getOrganizationId());
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
    public List<DepartmentOfOrganizationDTO> getDepartmentNamesOfOrganization(Long organizationId) {
        return this.departmentRepository.getDepartmentNamesOfOrganization(organizationId);
    }

    @Override
    public Page<DepartmentsTableRecordDTO> getDepartmentsTableData(Long organizationId, Pageable pageable) {
        return departmentRepository.getDepartmentsTableData(organizationId, pageable);
    }

    @Override
    public void registerDepartments(List<RegisterDepartmentDTO> departmentsToRegister, Long organizationId) throws DepartmentAlreadyExistsException {
        List<Department> departments = new ArrayList<>();
        for (RegisterDepartmentDTO departmentToRegister : departmentsToRegister) {
            boolean exists = departmentRepository.existsByDepartmentName(departmentToRegister.getDepartmentName().toLowerCase(), organizationId);
            if (exists) {
                throw new DepartmentAlreadyExistsException("Department: " + departmentToRegister.getDepartmentName() + " already exists");
            }
            departments.add(
                    Department.builder()
                            .departmentName(departmentToRegister.getDepartmentName())
                            .organization(
                                    Organization.builder()
                                            .id(organizationId)
                                            .build()
                            )
                            .build()
            );
        }
        departmentRepository.saveAll(departments);
    }
}

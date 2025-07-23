package com.main.face_recognition_resource_server.services.organization;

import com.main.face_recognition_resource_server.DTOS.department.DepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.department.OrganizationDepartmentDTO;
import com.main.face_recognition_resource_server.DTOS.leave.LeavesAllowedPolicyDTO;
import com.main.face_recognition_resource_server.DTOS.organization.OrganizationDTO;
import com.main.face_recognition_resource_server.DTOS.organization.RegisterOrganizationDTO;
import com.main.face_recognition_resource_server.converters.OrganizationToRegisterOrganizationDTOConvertor;
import com.main.face_recognition_resource_server.domains.Organization;
import com.main.face_recognition_resource_server.domains.OrganizationPolicies;
import com.main.face_recognition_resource_server.exceptions.OrganizationDoesntExistException;
import com.main.face_recognition_resource_server.repositories.OrganizationRepository;
import com.main.face_recognition_resource_server.services.organizationpolicies.OrganizationPoliciesServices;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class OrganizationServicesImpl implements OrganizationServices {
    private final OrganizationRepository organizationRepository;
    private final OrganizationPoliciesServices organizationPoliciesServices;

    public OrganizationServicesImpl(OrganizationRepository organizationRepository, OrganizationPoliciesServices organizationPoliciesServices) {
        this.organizationRepository = organizationRepository;
        this.organizationPoliciesServices = organizationPoliciesServices;
    }

    @Override
    public boolean organizationExists(Long organizationId) throws OrganizationDoesntExistException {
        boolean exists = organizationRepository.existsById(organizationId);
        if (!exists) {
            throw new OrganizationDoesntExistException();
        } else {
            return true;
        }
    }

    @Override
    public void registerOrganization(RegisterOrganizationDTO organizationToRegister) {
        Organization organization = OrganizationToRegisterOrganizationDTOConvertor.convert(organizationToRegister);
        OrganizationPolicies organizationPolicies = organizationPoliciesServices.saveOrganizationPolicies(organizationToRegister.getOrganizationPolicies());
        organization.setOrganizationPolicies(organizationPolicies);
        organizationRepository.saveAndFlush(organization);
    }

    @Override
    public Page<OrganizationDTO> getAllOrganizations(Pageable pageable) {
        return organizationRepository.getAllOrganizations(pageable);
    }

    @Override
    public OrganizationDTO getOrganizationDTO(Long id) throws OrganizationDoesntExistException {
        Optional<OrganizationDTO> organizationDTO = organizationRepository.getOrganizationById(id);
        if (organizationDTO.isEmpty()) {
            throw new OrganizationDoesntExistException();
        } else {
            return organizationDTO.get();
        }
    }

    @Override
    public Organization getOrganization(Long id) throws OrganizationDoesntExistException {
        Optional<Organization> organization = organizationRepository.findById(id);
        if (organization.isEmpty()) {
            throw new OrganizationDoesntExistException();
        } else {
            return organization.get();
        }
    }

    @Override
    public Page<OrganizationDepartmentDTO> getAllOrganizationsWithItsDepartments(Pageable pageable) {
        Page<Organization> organizationPage = organizationRepository.getAllOrganizationsWithDepartments(pageable);
        return organizationPage.map(organization -> {
            List<DepartmentDTO> departments = organization.getDepartments().stream().map(department -> new DepartmentDTO(department.getId(), department.getDepartmentName())).toList();
            return new OrganizationDepartmentDTO(organization.getId(), organization.getOrganizationName(), organization.getOrganizationType(), departments);
        });
    }

    @Override
    public int getOrganizationLateAttendanceToleranceTimePolicy(Long organizationId) {
        return organizationRepository.getLateAttendanceToleranceTimePolicy(organizationId);
    }

    @Override
    public LeavesAllowedPolicyDTO getOrganizationLeavesPolicy(Long organizationId) {
        return organizationRepository.getOrganizationLeavesAllowedPolicies(organizationId);
    }

    @Override
    public List<Long> getAllOrganizationIds() {
        return organizationRepository.getAllOrganizationIds();
    }

}

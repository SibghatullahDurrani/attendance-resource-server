package com.main.face_recognition_resource_server.services.organizationpolicies;

import com.main.face_recognition_resource_server.DTOS.organization.RegisterOrganizationPoliciesDTO;
import com.main.face_recognition_resource_server.domains.OrganizationPolicies;
import com.main.face_recognition_resource_server.repositories.OrganizationPoliciesRepository;
import org.springframework.stereotype.Service;

@Service
public class OrganizationPoliciesServiceImpl implements OrganizationPoliciesService {
    private final OrganizationPoliciesRepository organizationPoliciesRepository;

    public OrganizationPoliciesServiceImpl(OrganizationPoliciesRepository organizationPoliciesRepository) {
        this.organizationPoliciesRepository = organizationPoliciesRepository;
    }

    @Override
    public OrganizationPolicies saveOrganizationPolicies(RegisterOrganizationPoliciesDTO organizationPoliciesDTO) {
        OrganizationPolicies organizationPolicies = OrganizationPolicies.builder()
                .sickLeavesAllowed(organizationPoliciesDTO.getSickLeavesAllowed())
                .annualLeavesAllowed(organizationPoliciesDTO.getAnnualLeavesAllowed())
                .lateAttendanceToleranceTimeInMinutes(organizationPoliciesDTO.getLateAttendanceToleranceTimeInMinutes())
                .build();

        return organizationPoliciesRepository.saveAndFlush(organizationPolicies);
    }
}

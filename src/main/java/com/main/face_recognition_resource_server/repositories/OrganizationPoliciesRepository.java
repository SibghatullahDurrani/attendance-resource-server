package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.domains.OrganizationPolicies;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationPoliciesRepository extends JpaRepository<OrganizationPolicies, Long> {

}

package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.domains.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

}

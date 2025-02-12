package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.DTOS.OrganizationDTO;
import com.main.face_recognition_resource_server.domains.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.OrganizationDTO(
          o.id,o.organizationName,o.organizationType
          ) FROM Organization o
          """)
  Page<OrganizationDTO> getAllOrganizations(Pageable pageable);

  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.OrganizationDTO(
          o.id,o.organizationName,o.organizationType
          ) FROM Organization o WHERE o.id = ?1
          """)
  Optional<OrganizationDTO> getOrganizationById(Long id);

  @Query("""
          SELECT o FROM Organization o
          """)
  /*
  HACK: selecting the entire entity and all its data. now it is required
  but in the future if the entity changes and more data is available
  I will have to change this...
  */
  Page<Organization> getAllOrganizationsWithDepartments(Pageable pageable);
}

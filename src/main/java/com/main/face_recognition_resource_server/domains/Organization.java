package com.main.face_recognition_resource_server.domains;

import com.main.face_recognition_resource_server.constants.OrganizationTypes;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "organizations")
public class Organization {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organization_id_generator")
  @SequenceGenerator(name = "organization_id_generator", sequenceName = "organization_id_sequence", allocationSize = 1)
  private Long id;

  @Column(nullable = false)
  private String organizationName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrganizationTypes organizationType;

  @OneToMany(mappedBy = "organization")
  private List<Department> departments;
}

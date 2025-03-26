package com.main.face_recognition_resource_server.domains;

import com.main.face_recognition_resource_server.constants.OrganizationType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
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
  private OrganizationType organizationType;

  @OneToMany(mappedBy = "organization")
  private List<Department> departments;

  @OneToMany(mappedBy = "organization")
  private List<Camera> cameras;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "organization_policies_id", referencedColumnName = "id")
  private OrganizationPolicies organizationPolicies;
}

package com.main.face_recognition_resource_server.domains;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@Entity
@Table(name = "departments")
@NoArgsConstructor
public class Department {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "department_id_generator")
  @SequenceGenerator(name = "department_id_generator", sequenceName = "department_id_sequence", allocationSize = 1)
  private Long id;

  @Column(nullable = false)
  private String departmentName;

  @ManyToOne
  @JoinColumn(name = "organization_id", nullable = false)
  private Organization organization;

  @OneToMany(mappedBy = "department")
  private List<User> users;

  @ManyToMany(mappedBy = "departments")
  private List<Camera> cameras;
}

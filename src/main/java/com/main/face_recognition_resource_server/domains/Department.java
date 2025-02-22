package com.main.face_recognition_resource_server.domains;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@Entity
@Getter
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
}

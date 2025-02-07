package com.main.face_recognition_resource_server.domains;

import com.main.face_recognition_resource_server.constants.UserRole;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_generator")
  @SequenceGenerator(name = "user_id_generator", sequenceName = "user_id_sequence", allocationSize = 1)
  private Long id;

  @Column(name = "first_name", nullable = false, length = 30)
  private String firstName;

  @Column(name = "second_name", nullable = false, length = 30)
  private String secondName;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "username", nullable = false, unique = true)
  private String username;
  //TODO: generate unique usernames

  @Column(name = "role", nullable = false, length = 30)
  @Enumerated(EnumType.STRING)
  private UserRole role;

  @Column(nullable = false)
  private String identificationNumber;

  @Column(nullable = false)
  private String email;

  @ManyToOne
  @JoinColumn(name = "department_id", nullable = false)
  private Department department;
}

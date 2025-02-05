package com.main.face_recognition_resource_server.domains;

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

  @Column(name = "role", nullable = false, length = 30)
  private String role;
}

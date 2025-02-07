package com.main.face_recognition_resource_server.domains;

import com.main.face_recognition_resource_server.constants.CameraTypes;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "cameras")
public class Camera {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "camera_id_generator")
  @SequenceGenerator(name = "camera_id_generator", sequenceName = "camera_id__sequence", allocationSize = 1)
  private Long id;

  @Column(name = "ip_address", unique = true, nullable = false, length = 15)
  private String ipAddress;

  private int port;

  @Column(nullable = false)
  private String username;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false, length = 3)
  @Enumerated(EnumType.STRING)
  private CameraTypes type;
}

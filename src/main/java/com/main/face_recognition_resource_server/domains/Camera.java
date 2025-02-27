package com.main.face_recognition_resource_server.domains;

import com.main.face_recognition_resource_server.constants.CameraStatus;
import com.main.face_recognition_resource_server.constants.CameraType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder
@Entity
@Table(name = "cameras")
public class Camera {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "camera_id_generator")
  @SequenceGenerator(name = "camera_id_generator", sequenceName = "camera_id__sequence", allocationSize = 1)
  private Long id;

  @Column(name = "ip_address", nullable = false, length = 15)
  private String ipAddress;

  private int port;

  private int channel;

  @Column(nullable = false)
  private String username;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false, length = 3)
  @Enumerated(EnumType.STRING)
  private CameraType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CameraStatus cameraStatus;

  @ManyToOne
  @JoinColumn(name = "organization_id")
  private Organization organization;
}

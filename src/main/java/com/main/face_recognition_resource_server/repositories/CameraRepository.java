package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.DTOS.CameraCredentialsDTO;
import com.main.face_recognition_resource_server.DTOS.GetCameraDTO;
import com.main.face_recognition_resource_server.constants.CameraStatus;
import com.main.face_recognition_resource_server.domains.Camera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CameraRepository extends JpaRepository<Camera, Long> {
  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.GetCameraDTO(
          c.id, c.ipAddress, c.port, c.channel, c.type, c.cameraStatus
          ) FROM Camera c JOIN c.organization o WHERE o.id = ?1
          """)
  List<GetCameraDTO> getCamerasOfOrganization(Long organizationId);

  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.CameraCredentialsDTO(
          c.id,c.ipAddress,c.port,c.channel,c.username,c.password,c.type
          ) FROM Camera c JOIN c.organization o WHERE o.id = ?1
          """)
  List<CameraCredentialsDTO> getCameraCredentialsOfOrganization(Long organizationId);

  @Query("""
          UPDATE Camera c SET c.cameraStatus = ?2 WHERE c.id = ?1
          """)
  @Modifying
  @Transactional
  void setCameraStatusOfCamera(Long id, CameraStatus cameraStatus);

  @Query("""
          SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END FROM Camera c
          WHERE c.ipAddress = ?1 AND c.port = ?2 AND c.channel = ?3
          """)
  boolean existsByIpAddressAndPortAndChannel(String ipAddress, int port, int channel);
}

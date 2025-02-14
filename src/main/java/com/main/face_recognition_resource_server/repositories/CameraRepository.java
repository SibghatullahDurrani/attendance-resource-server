package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.DTOS.DepartmentCameraDTO;
import com.main.face_recognition_resource_server.constants.CameraType;
import com.main.face_recognition_resource_server.domains.Camera;
import org.apache.ibatis.annotations.Options;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CameraRepository extends JpaRepository<Camera, Long> {
  @Query("""
          SELECT camera FROM Camera camera WHERE camera.ipAddress = ?1 and camera.port = ?2
          """)
  Optional<Camera> getCameraByIpAddressAndPort(String ipAddress, int port);

  @Query("""
          SELECT c FROM Camera c WHERE c.ipAddress = ?1 AND c.port = ?2 AND c.channel = ?3
          """)
  Optional<Camera> getCameraByIpAddressPortAndChannel(String ipAddress, int port, int channel);

  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.DepartmentCameraDTO(
          c.id, c.ipAddress, c.port, c.channel, c.type, c.cameraStatus
          ) FROM Camera c JOIN c.departments d WHERE d.id = ?1
          """)
  List<DepartmentCameraDTO> getCamerasOfDepartment(Long departmentId);

  @Query("""
          SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END FROM Camera c JOIN c.departments d
          WHERE c.ipAddress = ?1 AND c.port = ?2 AND c.channel = ?3 AND d.id = ?4
          """)
  boolean existsByIpAddressAndPortAndChannelAndDepartmentId(String ipAddress, int port, int channel, Long departmentId);
}

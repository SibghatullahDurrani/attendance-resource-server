package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.DTOS.DepartmentCameraDTO;
import com.main.face_recognition_resource_server.domains.Camera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CameraRepository extends JpaRepository<Camera, Long> {
  @Query("""
          SELECT camera FROM Camera camera WHERE camera.ipAddress = ?1 and camera.port = ?2
          """)
  Optional<Camera> getCameraByIpAddressAndPort(String ipAddress, int port);

  @Query("""
          SELECT new com.main.face_recognition_resource_server.DTOS.DepartmentCameraDTO(
          c.id, c.ipAddress, c.port, c.channel, c.type, c.cameraStatus
          ) FROM Camera c JOIN c.departments d WHERE d.id = ?1
          """)
  List<DepartmentCameraDTO> getCamerasOfDepartment(Long departmentId);
}

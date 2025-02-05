package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.domains.Camera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CameraRepository extends JpaRepository<Camera, Long> {
  @Query("""
          SELECT camera FROM Camera camera WHERE camera.ipAddress = ?1 and camera.port = ?2
          """)
  Optional<Camera> findCameraByIpAddressAndPort(String ipAddress, int port);
}

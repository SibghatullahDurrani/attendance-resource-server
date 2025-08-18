package com.main.face_recognition_resource_server.repositories;

import com.main.face_recognition_resource_server.domains.UserShiftSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserShiftRepository extends JpaRepository<UserShiftSetting, Long> {
}

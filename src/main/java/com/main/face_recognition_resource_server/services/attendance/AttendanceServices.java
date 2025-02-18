package com.main.face_recognition_resource_server.services.attendance;

import com.main.face_recognition_resource_server.exceptions.UserDoesntExistException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;

public interface AttendanceServices {
  void markAttendance(Long userId, Date date, BufferedImage image) throws UserDoesntExistException, IOException;
}

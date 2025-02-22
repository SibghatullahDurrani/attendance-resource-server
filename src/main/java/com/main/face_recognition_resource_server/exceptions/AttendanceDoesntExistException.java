package com.main.face_recognition_resource_server.exceptions;

public class AttendanceDoesntExistException extends Exception {
  public AttendanceDoesntExistException() {
    super();
  }

  public AttendanceDoesntExistException(String message) {
    super(message);
  }
}

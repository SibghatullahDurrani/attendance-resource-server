package com.main.face_recognition_resource_server.exceptions;

public class CameraAlreadyExistsInDepartmentException extends RuntimeException {
  public CameraAlreadyExistsInDepartmentException() {
    super();
  }

  CameraAlreadyExistsInDepartmentException(String message) {
    super(message);
  }
}

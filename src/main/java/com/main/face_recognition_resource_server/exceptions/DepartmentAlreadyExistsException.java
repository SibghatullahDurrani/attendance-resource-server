package com.main.face_recognition_resource_server.exceptions;

public class DepartmentAlreadyExistsException extends Exception {
  public DepartmentAlreadyExistsException() {
    super();
  }
  public DepartmentAlreadyExistsException(String message) {
    super(message);
  }
}

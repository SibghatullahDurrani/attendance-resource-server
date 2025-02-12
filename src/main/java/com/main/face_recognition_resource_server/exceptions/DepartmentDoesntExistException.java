package com.main.face_recognition_resource_server.exceptions;

public class DepartmentDoesntExistException extends RuntimeException {
  public DepartmentDoesntExistException() {
    super("The department specified doesnt exist");
  }
}

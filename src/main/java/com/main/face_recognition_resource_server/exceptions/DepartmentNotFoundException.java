package com.main.face_recognition_resource_server.exceptions;

public class DepartmentNotFoundException extends RuntimeException {
  public DepartmentNotFoundException() {
    super("The department specified doesnt exist");
  }
}

package com.main.face_recognition_resource_server.exceptions;

public class DepartmentDoesntExistException extends Exception {
  public DepartmentDoesntExistException() {
    super("The department specified doesnt exist");
  }
}

package com.main.face_recognition_resource_server.exceptions;

public class OrganizationDoesntExistException extends Exception {
  public OrganizationDoesntExistException() {
    super();
  }

  public OrganizationDoesntExistException(String message) {
    super(message);
  }
}

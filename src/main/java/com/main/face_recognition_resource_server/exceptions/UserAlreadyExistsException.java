package com.main.face_recognition_resource_server.exceptions;

public class UserAlreadyExistsException extends Exception {
  public UserAlreadyExistsException() {
    super();
  }

  public UserAlreadyExistsException(String message) {
    super(message);
  }
}

package com.main.face_recognition_resource_server.exceptions;

public class UserAlreadyExistsWithIdentificationNumberException extends Exception {
  public UserAlreadyExistsWithIdentificationNumberException() {
    super();
  }

  public UserAlreadyExistsWithIdentificationNumberException(String message) {
    super(message);
  }
}

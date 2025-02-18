package com.main.face_recognition_resource_server.exceptions;

public class NoInCameraExistsException extends Exception {
  public NoInCameraExistsException() {
    super();
  }

  public NoInCameraExistsException(String message) {
    super(message);
  }
}

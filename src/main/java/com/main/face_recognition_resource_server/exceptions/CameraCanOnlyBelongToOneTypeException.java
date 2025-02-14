package com.main.face_recognition_resource_server.exceptions;

public class CameraCanOnlyBelongToOneTypeException extends RuntimeException {
  public CameraCanOnlyBelongToOneTypeException() {
    super();
  }

  public CameraCanOnlyBelongToOneTypeException(String message) {
    super(message);
  }
}

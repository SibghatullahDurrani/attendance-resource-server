package com.main.face_recognition_resource_server.exceptions;

public class LeaveDoesntExistException extends Exception {
  public LeaveDoesntExistException() {
    super();
  }

  public LeaveDoesntExistException(String message) {
    super(message);
  }
}

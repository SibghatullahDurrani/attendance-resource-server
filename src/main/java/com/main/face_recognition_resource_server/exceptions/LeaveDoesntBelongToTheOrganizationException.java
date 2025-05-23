package com.main.face_recognition_resource_server.exceptions;

public class LeaveDoesntBelongToTheOrganizationException extends Exception {
  public LeaveDoesntBelongToTheOrganizationException() {
    super();
  }

  public LeaveDoesntBelongToTheOrganizationException(String message) {
    super(message);
  }
}

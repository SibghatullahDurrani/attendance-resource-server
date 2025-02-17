package com.main.face_recognition_resource_server.exceptions;

public class OrganizationDoesntBelongToYouException extends Exception {
  public OrganizationDoesntBelongToYouException() {
    super();
  }

  public OrganizationDoesntBelongToYouException(String message) {
    super(message);
  }
}

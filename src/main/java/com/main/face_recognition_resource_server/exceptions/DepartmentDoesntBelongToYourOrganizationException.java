package com.main.face_recognition_resource_server.exceptions;

public class DepartmentDoesntBelongToYourOrganizationException extends RuntimeException {
  public DepartmentDoesntBelongToYourOrganizationException() {
    super("The department specified doesnt belong to your organization");
  }
}

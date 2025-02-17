package com.main.face_recognition_resource_server.exceptions;

public class DepartmentDoesntBelongToYourOrganizationException extends Exception {
  public DepartmentDoesntBelongToYourOrganizationException() {
    super("The department specified doesnt belong to your organization");
  }
}

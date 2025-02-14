package com.main.face_recognition_resource_server.exceptions;

public class CameraCanOnlyBelongToOrganizationOrDepartmentException extends RuntimeException {
  public CameraCanOnlyBelongToOrganizationOrDepartmentException() {
    super();
  }

  public CameraCanOnlyBelongToOrganizationOrDepartmentException(String message) {
    super(message);
  }
}

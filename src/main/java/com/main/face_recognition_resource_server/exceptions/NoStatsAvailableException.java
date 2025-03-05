package com.main.face_recognition_resource_server.exceptions;

public class NoStatsAvailableException extends Exception {
  public NoStatsAvailableException(String msg) {
    super(msg);
  }

  public NoStatsAvailableException() {
    super();
  }
}

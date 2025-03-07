package com.main.face_recognition_resource_server.controllers.advices;

import com.main.face_recognition_resource_server.DTOS.ErrorDTO;
import com.main.face_recognition_resource_server.controllers.AttendanceController;
import com.main.face_recognition_resource_server.exceptions.NoStatsAvailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(assignableTypes = {AttendanceController.class})
public class AttendanceControllerAdvice {

  @ExceptionHandler({NoStatsAvailableException.class})
  protected ResponseEntity<ErrorDTO> handleNoStatsAvailableException(NoStatsAvailableException ex) {
    return new ResponseEntity<>(ErrorDTO.builder()
            .message("No Stats Found!")
            .build(), HttpStatus.NOT_FOUND);
  }
}

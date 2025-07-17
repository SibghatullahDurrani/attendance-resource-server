package com.main.face_recognition_resource_server.controllers.advices;

import com.main.face_recognition_resource_server.DTOS.ErrorDTO;
import com.main.face_recognition_resource_server.controllers.DepartmentController;
import com.main.face_recognition_resource_server.exceptions.DepartmentAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(assignableTypes = {DepartmentController.class})
public class DepartmentControllerAdvice {

  @ExceptionHandler({DepartmentAlreadyExistsException.class})
  protected ResponseEntity<ErrorDTO> handleDepartmentDoestExistException(DepartmentAlreadyExistsException exception) {
    return new ResponseEntity<>(ErrorDTO.builder()
            .message(exception.getMessage())
            .build(), HttpStatus.BAD_REQUEST);
  }
}

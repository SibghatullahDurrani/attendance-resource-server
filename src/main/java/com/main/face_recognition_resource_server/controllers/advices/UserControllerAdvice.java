package com.main.face_recognition_resource_server.controllers.advices;

import com.main.face_recognition_resource_server.DTOS.ErrorDTO;
import com.main.face_recognition_resource_server.controllers.UserController;
import com.main.face_recognition_resource_server.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

@ControllerAdvice(assignableTypes = {UserController.class})
public class UserControllerAdvice {

    @ExceptionHandler({DepartmentDoesntExistException.class})
    protected ResponseEntity<ErrorDTO> handleDepartmentDoestExistException(DepartmentDoesntExistException exception) {
        return new ResponseEntity<>(ErrorDTO.builder()
                .message("Department Specified Doesn't Exist!")
                .build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({UserDoesntExistException.class})
    protected ResponseEntity<ErrorDTO> handleUserDoesntExistException(UserDoesntExistException exception) {
        return new ResponseEntity<>(ErrorDTO.builder()
                .message("something went wrong!")
                .build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({DepartmentDoesntBelongToYourOrganizationException.class})
    protected ResponseEntity<ErrorDTO> handleUserDoesntExistException(DepartmentDoesntBelongToYourOrganizationException exception) {
        return new ResponseEntity<>(ErrorDTO.builder()
                .message("something went wrong!")
                .build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({UserAlreadyExistsException.class})
    protected ResponseEntity<ErrorDTO> handleUserAlreadyExistsException(UserAlreadyExistsException exception) {
        return new ResponseEntity<>(ErrorDTO.builder()
                .message("This user already exists with this email")
                .build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({UserAlreadyExistsWithIdentificationNumberException.class})
    protected ResponseEntity<ErrorDTO> handleUserAlreadyExistsWithIdentificationNumberException(UserAlreadyExistsWithIdentificationNumberException exception) {
        return new ResponseEntity<>(ErrorDTO.builder()
                .message(exception.getMessage())
                .build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({IOException.class})
    protected ResponseEntity<ErrorDTO> handleUserAlreadyExistsException(IOException exception) {
        return new ResponseEntity<>(ErrorDTO.builder()
                .message(exception.getMessage())
                .build(), HttpStatus.BAD_REQUEST);
    }
}

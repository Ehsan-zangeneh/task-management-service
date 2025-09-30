package com.task.management.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

import java.time.ZonedDateTime;

@RestControllerAdvice
@Slf4j
public class TaskManagementControllerExceptionHandler {

    @ExceptionHandler({TaskNotFoundException.class})
    public ResponseEntity<ErrorMessage> handleTaskNotFoundException(TaskNotFoundException e) {
        return buildErrorMessage(HttpStatus.NOT_FOUND, e.getMessage(), e);
    }

    @ExceptionHandler({WebExchangeBindException.class})
    public ResponseEntity<ErrorMessage> handleWebExchangeBindException(WebExchangeBindException e) {
        var errors = e.getFieldErrors().stream()
                .map(error -> String.format("Field '%s': %s", error.getField(), error.getDefaultMessage()))
                .toList();
        var message = errors.toString();
        return buildErrorMessage(HttpStatus.BAD_REQUEST, message,e);
    }

    @ExceptionHandler({ConstraintViolationException.class, ServerWebInputException.class})
    public ResponseEntity<ErrorMessage> handleConstraintViolationException(Exception e) {
        var message = e.getMessage();
        return buildErrorMessage(HttpStatus.BAD_REQUEST, message,e);
    }

    @ExceptionHandler({IllegalTaskManagementOperationException.class})
    public ResponseEntity<ErrorMessage> handleIllegalTaskManagementOperationException(IllegalTaskManagementOperationException e) {
        return buildErrorMessage(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorMessage> handleException(Exception e) {
        return buildErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, "An Internal Error occurred",e);
    }


    private ResponseEntity<ErrorMessage> buildErrorMessage(HttpStatus httpStatus, String message, Exception e) {
        log.error(message, e);
        var errorBody = ErrorMessage.builder()
                .message(message)
                .status(httpStatus.value())
                .description(httpStatus.getReasonPhrase())
                .timestamp(ZonedDateTime.now())
                .build();
        return ResponseEntity.status(errorBody.getStatus()).body(errorBody);
    }

}

package com.task.management.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebInputException;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class TaskManagementControllerExceptionHandler {

    @ExceptionHandler({TaskNotFoundException.class})
    public ErrorMessage handleTaskNotFoundException(TaskNotFoundException e) {
        return buildErrorMessage(HttpStatus.NOT_FOUND, e.getMessage(), e);
    }

    @ExceptionHandler({ConstraintViolationException.class, ServerWebInputException.class})
    public ErrorMessage handleConstraintViolationException(Exception e) {
        var message = Arrays.stream(e.getMessage().split(","))
                .map(String::trim)
                .map(s -> s.substring(s.indexOf(".") + 1))
                .map("[%s]"::formatted)
                .collect(Collectors.joining(" "));

        return buildErrorMessage(HttpStatus.BAD_REQUEST, message,e);
    }

    @ExceptionHandler({IllegalTaskManagementOperationException.class})
    public ErrorMessage handleIllegalTaskManagementOperationException(IllegalTaskManagementOperationException e) {
        return buildErrorMessage(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }

    @ExceptionHandler({Exception.class})
    public ErrorMessage handleException(Exception e) {
        return buildErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(),e);
    }


    private ErrorMessage buildErrorMessage(HttpStatus httpStatus, String message, Exception e) {
        log.error(message, e);
        return ErrorMessage.builder()
                .message(message)
                .status(httpStatus.value())
                .description(httpStatus.getReasonPhrase())
                .timestamp(ZonedDateTime.now())
                .build();
    }


}

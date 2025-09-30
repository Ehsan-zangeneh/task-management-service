package com.task.management.exception;

import lombok.Builder;
import lombok.Value;

import java.time.ZonedDateTime;

@Builder
@Value
public class ErrorMessage {
    int status;
    String description;
    String message;
    ZonedDateTime timestamp;
}

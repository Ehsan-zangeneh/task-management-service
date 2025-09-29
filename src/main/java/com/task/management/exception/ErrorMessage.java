package com.task.management.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

import java.time.ZonedDateTime;

@Builder
@Value
@Schema(name = "ErrorMessage")
public class ErrorMessage {
    int status;
    String description;
    String message;
    ZonedDateTime timestamp;
}

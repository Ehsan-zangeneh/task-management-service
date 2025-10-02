package com.task.management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Builder
@Value
public class TaskUpdateDto {
    @NotNull
    UUID id;
    TaskUpdateRequestDto taskUpdateRequestDto;
}

package com.task.management.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Builder
@Value
public class TaskUpdateDto {
    @NotBlank
    UUID id;
    TaskUpdateRequestDto taskUpdateRequestDto;
}

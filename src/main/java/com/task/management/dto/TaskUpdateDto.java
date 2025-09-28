package com.task.management.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class TaskUpdateDto {
    @NotBlank
    String id;
    TaskUpdateRequestDto taskUpdateRequestDto;
}

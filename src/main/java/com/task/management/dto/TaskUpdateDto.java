package com.task.management.dto;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class TaskUpdateDto {
    String id;
    TaskUpdateRequestDto taskUpdateRequestDto;
}

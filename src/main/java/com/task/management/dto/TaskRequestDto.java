package com.task.management.dto;

import lombok.Builder;
import lombok.Value;


@Builder
@Value
public class TaskRequestDto {

    String title;
    String description;
    String ownerId;
    String assigneeId;

}

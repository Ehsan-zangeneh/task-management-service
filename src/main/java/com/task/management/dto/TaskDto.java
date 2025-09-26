package com.task.management.dto;

import com.task.management.model.TaskStatus;
import lombok.Builder;
import lombok.Value;

import java.time.ZonedDateTime;

@Builder
@Value
public class TaskDto {

    String id;
    String title;
    String description;
    ZonedDateTime creationDate;
    ZonedDateTime modificationDate;
    TaskStatus status;
    String ownerId;
    String assigneeId;

}

package com.task.management.dto;

import com.task.management.common.TaskConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Value;


@Builder
@Value
public class TaskUpdateRequestDto {

    @NotBlank
    String title;
    String description;
    @NotBlank(message = "Owner cannot be null")
    @Pattern(regexp = TaskConstants.UUID, message = "Must be a valid UUID")
    String ownerId;
    @Pattern(regexp = TaskConstants.UUID, message = "Must be a valid UUID")
    String assigneeId;

}

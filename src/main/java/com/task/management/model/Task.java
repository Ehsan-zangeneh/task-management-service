package com.task.management.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder(toBuilder = true)
@Table("task")
public class Task {

    @Id
    UUID id;
    String title;
    String description;
    ZonedDateTime creationDate;
    ZonedDateTime modificationDate;
    TaskStatus status;
    String ownerId;
    String assigneeId;

}

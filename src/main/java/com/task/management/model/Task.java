package com.task.management.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.ZonedDateTime;

@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder(toBuilder = true)
@Entity
@SQLDelete(sql = "UPDATE task SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String title;
    String description;
    ZonedDateTime creationDate;
    ZonedDateTime modificationDate;
    TaskStatus status;
    boolean isDeleted;
    ZonedDateTime deleteDate;
    String ownerId;
    String assigneeId;

    @PrePersist
    protected void prePersist() {
        this.creationDate = ZonedDateTime.now();
        status = TaskStatus.Todo;
    }

    @PreUpdate
    protected void preUpdate() {
        this.modificationDate = ZonedDateTime.now();
    }

}

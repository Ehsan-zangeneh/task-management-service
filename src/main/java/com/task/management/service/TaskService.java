package com.task.management.service;

import com.task.management.dto.TaskCreateRequestDto;
import com.task.management.dto.TaskDto;
import com.task.management.dto.TaskUpdateDto;
import com.task.management.dto.TaskUpdateRequestDto;
import com.task.management.exception.IllegalTaskManagementOperationException;
import com.task.management.exception.TaskNotFoundException;
import com.task.management.model.Task;
import com.task.management.model.TaskStatus;
import com.task.management.repository.TaskRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class TaskService {

    private final TaskRepository taskRepository;

    public Flux<TaskDto> findAll(int page, int size) {
        var offset = page * size;
        var tasks = taskRepository.findAllPaged(size, offset);
        return tasks.map(this::convertToDto);
    }

    public Mono<TaskDto> findById(UUID taskId) {
        log.info("Find task by id {}", taskId);
        return taskRepository.findById(taskId)
                .switchIfEmpty(Mono.error(
                        new TaskNotFoundException("Task not found with id:{%s}".formatted(taskId))
                ))
                .map(this::convertToDto);
    }

    public Mono<TaskDto> save(@Valid TaskCreateRequestDto taskCreationRequestDto) {
        log.info("Save task {}", taskCreationRequestDto);
        var task = taskRepository.save(Task.builder()
                .assigneeId(taskCreationRequestDto.getAssigneeId())
                .description(taskCreationRequestDto.getDescription())
                .ownerId(taskCreationRequestDto.getOwnerId())
                .title(taskCreationRequestDto.getTitle())
                .creationDate(ZonedDateTime.now())
                .status(TaskStatus.TODO)
                .build());
        return task.map(this::convertToDto);
    }

    public Mono<TaskDto> update(@Valid TaskUpdateDto taskUpdateDto) {
        log.info("Update task {}", taskUpdateDto);
        return taskRepository.findById(taskUpdateDto.getId())
                .switchIfEmpty(Mono.error(
                        new TaskNotFoundException("Task not found with id:{%s}".formatted(taskUpdateDto.getId()))
                ))
                .map(t -> merge(t, taskUpdateDto.getTaskUpdateRequestDto()))
                .flatMap(taskRepository::save)
                .map(this::convertToDto);
    }

    public Mono<String> delete(UUID taskId) {
        log.info("Delete task by id {}", taskId);
        return taskRepository.findById(taskId)
                .switchIfEmpty(Mono.error(new TaskNotFoundException("Task not found with id:{%s}".formatted(taskId))))
                .flatMap(task -> {
                    if (!checkValidityForRemove(task)) {
                        return Mono.error(new IllegalTaskManagementOperationException(
                                "The task with id:{%s} not valid for deletion".formatted(task.getId())
                        ));
                    }
                    return taskRepository.deleteById(taskId).thenReturn(taskId.toString());
                });
    }


    private boolean checkValidityForRemove(Task task) {
        return task.getStatus().equals(TaskStatus.TODO)
                || task.getStatus().equals(TaskStatus.CANCELLED);
    }

    private Task merge(Task task, TaskUpdateRequestDto taskUpdateRequestDto) {
        var taskBuilder = task.toBuilder()
                .assigneeId(taskUpdateRequestDto.getAssigneeId())
                .description(taskUpdateRequestDto.getDescription())
                .title(taskUpdateRequestDto.getTitle())
                .modificationDate(ZonedDateTime.now());
        if (taskUpdateRequestDto.getStatus() != null) {
                taskBuilder.status(TaskStatus.valueOf(taskUpdateRequestDto.getStatus().getValue()));

        }
        return taskBuilder.build();
    }

    private TaskDto convertToDto(Task task) {
        log.info("Converting task {}", task);
        return TaskDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(com.task.management.dto.TaskStatus.valueOf(task.getStatus().name()))
                .creationDate(task.getCreationDate())
                .modificationDate(task.getModificationDate())
                .assigneeId(task.getAssigneeId())
                .ownerId(task.getOwnerId())
                .build();
    }

}

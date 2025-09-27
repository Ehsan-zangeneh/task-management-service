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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class TaskService {

    private final TaskRepository taskRepository;

    public List<TaskDto> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(
                page, size,
                Sort.sort(Task.class).by(Task::getCreationDate).descending());

        var tasks = taskRepository.findAll(pageable);
        return tasks.stream()
                .map(this::convertToDto)
                .toList();
    }

    public TaskDto findById(String taskId) {
        log.info("Find task by id {}", taskId);
        return taskRepository.findById(taskId)
                .map(this::convertToDto)
                .orElseThrow(() -> new TaskNotFoundException("Task by id:{%s} not found".formatted(taskId)));
    }

    public TaskDto save(@Valid TaskCreateRequestDto taskCreationRequestDto) {
        log.info("Save task {}", taskCreationRequestDto);
        var task = taskRepository.save(Task.builder()
                .assigneeId(taskCreationRequestDto.getAssigneeId())
                .description(taskCreationRequestDto.getDescription())
                .ownerId(taskCreationRequestDto.getOwnerId())
                .title(taskCreationRequestDto.getTitle())
                .build());
        return convertToDto(task);
    }

    public TaskDto update(TaskUpdateDto taskUpdateDto) {
        log.info("Update task {}", taskUpdateDto);
        return taskRepository.findById(taskUpdateDto.getId())
                .map(t -> merge(t, taskUpdateDto.getTaskUpdateRequestDto()))
                .map(taskRepository::save)
                .map(this::convertToDto)
                .orElseThrow(() ->  new TaskNotFoundException("Task by id:{%s} not found".formatted(taskUpdateDto.getId())));
    }

    public String delete(String taskId) {
        log.info("Delete task by id {}", taskId);
        var task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException("Task by id:{%s} not found".formatted(taskId)));
        if(!checkValidityForRemove(task)) {
            throw new IllegalTaskManagementOperationException(
                    "The task {%s} not valid for deletion".formatted(task)
            );
        }
        taskRepository.deleteById(taskId);
        return taskId;
    }

    private boolean checkValidityForRemove(Task task) {
        return task.getStatus().equals(TaskStatus.Todo)
                || task.getStatus().equals(TaskStatus.Cancelled);
    }

    private Task merge(Task task, TaskUpdateRequestDto taskUpdateRequestDto) {
        return task.toBuilder()
                .assigneeId(taskUpdateRequestDto.getAssigneeId())
                .description(taskUpdateRequestDto.getDescription())
                .title(taskUpdateRequestDto.getTitle())
                .build();
    }

    private TaskDto convertToDto(Task task) {
        log.info("Converting task {}", task);
        return TaskDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .creationDate(task.getCreationDate())
                .modificationDate(task.getModificationDate())
                .assigneeId(task.getAssigneeId())
                .ownerId(task.getOwnerId())
                .build();
    }

}

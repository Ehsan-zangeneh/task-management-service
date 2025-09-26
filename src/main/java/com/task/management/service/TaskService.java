package com.task.management.service;

import com.task.management.dto.TaskCreationRequestDto;
import com.task.management.dto.TaskDto;
import com.task.management.model.Task;
import com.task.management.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;

    public List<TaskDto> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(
                page, size,
                Sort.sort(Task.class).by(Task::getCreationDate).descending());

        return taskRepository.findAll(pageable).stream()
                .map(this::convertToDto)
                .toList();
    }

    public TaskDto findById(String taskId) {
        log.info("Find task {}", taskId);
        return taskRepository.findById(taskId)
                .map(this::convertToDto)
                .orElseThrow(() -> new RuntimeException("Task by id:{%s} not found".formatted(taskId)));
    }

    public TaskDto save(TaskCreationRequestDto taskCreationRequestDto) {
        log.info("Save task {}", taskCreationRequestDto);
        var task = taskRepository.save(Task.builder()
                .assigneeId(taskCreationRequestDto.getAssigneeId())
                .description(taskCreationRequestDto.getDescription())
                .ownerId(taskCreationRequestDto.getOwnerId())
                .title(taskCreationRequestDto.getTitle())
                .build());
        return convertToDto(task);
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

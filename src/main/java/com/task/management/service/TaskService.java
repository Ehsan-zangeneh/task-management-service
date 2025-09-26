package com.task.management.service;

import com.task.management.dto.TaskDto;
import com.task.management.model.Task;
import com.task.management.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;

    public Flux<TaskDto> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(
                page, size,
                Sort.sort(Task.class).by(Task::getCreationDate).descending());

        return Mono.fromCallable(() -> taskRepository.findAll(pageable))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(
                        tasks -> Flux.fromStream(tasks.stream()
                                .map(this::convertToDto))
                );
    }

    public Mono<TaskDto> findById(String taskId) {
        return  Mono.fromCallable(() -> taskRepository.findById(taskId)
                        .map(this::convertToDto)
                        .orElseThrow(() -> new RuntimeException("Task by id:{%s} not found".formatted(taskId))))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private TaskDto convertToDto(Task task) {
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

package com.task.management.controller;

import com.task.management.api.TaskManagementApi;
import com.task.management.dto.TaskCreateRequestDto;
import com.task.management.dto.TaskDto;
import com.task.management.dto.TaskUpdateDto;
import com.task.management.dto.TaskUpdateRequestDto;
import com.task.management.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TaskController implements TaskManagementApi {

    private final TaskService taskService;

    @Override
    public Mono<ResponseEntity<Flux<TaskDto>>> getAllTasks(Integer page, Integer size, ServerWebExchange exchange) {
        var taskStream = taskService.findAll(page, size);
        return Mono.just(ResponseEntity.ok(taskStream));
    }

    @Override
    public Mono<ResponseEntity<TaskDto>> getTaskById(UUID id, ServerWebExchange exchange) {
        return taskService.findById(id)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<TaskDto>> createTask(Mono<TaskCreateRequestDto> taskCreateRequestDto, ServerWebExchange exchange) {
        return taskCreateRequestDto
                .map(taskService::save)
                .flatMap(taskDtoMono -> taskDtoMono.map(
                        taskDto -> ResponseEntity.status(HttpStatus.CREATED).body(taskDto)
                ));
    }

    @Override
    public Mono<ResponseEntity<TaskDto>> updateTaskById(UUID id, Mono<TaskUpdateRequestDto> taskUpdateRequestDto, ServerWebExchange exchange) {
        return taskUpdateRequestDto
                .map(updateRequest -> taskService.update(TaskUpdateDto.builder()
                        .id(id)
                        .taskUpdateRequestDto(updateRequest)
                        .build()))
                .flatMap(taskDtoMono -> taskDtoMono.map(ResponseEntity::ok));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteTaskById(UUID id, ServerWebExchange exchange) {
        log.info("Delete task by id {}", id);
        return taskService.delete(id)
                .map(deletedTaskId -> ResponseEntity.noContent().build());
    }

}

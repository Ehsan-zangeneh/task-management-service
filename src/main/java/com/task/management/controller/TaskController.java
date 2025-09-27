package com.task.management.controller;


import com.task.management.dto.TaskCreateRequestDto;
import com.task.management.dto.TaskDto;
import com.task.management.dto.TaskUpdateDto;
import com.task.management.dto.TaskUpdateRequestDto;
import com.task.management.exception.ErrorMessage;
import com.task.management.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Retrieve a paginated list of tasks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of tasks",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TaskDto.class))
                    )
            )
    })
    @GetMapping
    public Flux<TaskDto> findAll(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        log.info("Find all tasks with page {} and size {}", page, size);
        return taskService.findAll(page, size);
    }

    @Operation(summary = "Retrieve a task by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task retrieved successfully"),
            @ApiResponse(responseCode = "404",
                    description = "Task not found",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))
            ),
    })
    @GetMapping("/{id}")
    public Mono<ResponseEntity<TaskDto>> findById(@PathVariable String id) {
        log.info("Find task by id {}", id);
        return taskService.findById(id)
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Create a new task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
    })
    @PostMapping
    public Mono<ResponseEntity<TaskDto>> create(@RequestBody TaskCreateRequestDto taskCreationMono) {
        return taskService.save(taskCreationMono)
                .map(taskDto -> ResponseEntity.status(HttpStatus.CREATED).body(taskDto));
    }

    @Operation(summary = "Update a task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful update of a task",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TaskDto.class))
                    )
            )
    })
    @PutMapping("/{id}")
    public Mono<ResponseEntity<TaskDto>> update(@PathVariable String id, @RequestBody TaskUpdateRequestDto taskUpdateRequestDto) {
        log.info("Update task by id{} with data {}", id,  taskUpdateRequestDto);
        return taskService.update(TaskUpdateDto.builder()
                        .id(id)
                        .taskUpdateRequestDto(taskUpdateRequestDto)
                        .build())
                .map(ResponseEntity::ok);
    }

    @Operation(summary = "Delete a task by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
    })
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<String>> delete(@PathVariable String id) {
        log.info("Delete task by id {}", id);
        return taskService.delete(id)
                .map(deletedTaskId -> ResponseEntity.status(HttpStatus.NO_CONTENT).body(deletedTaskId));
    }

}

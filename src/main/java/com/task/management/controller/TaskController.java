package com.task.management.controller;


import com.task.management.api.TasksApi;
import com.task.management.dto.TaskDto;
import com.task.management.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/tasks")
public class TaskController implements TasksApi {

    private final TaskService taskService;

    @Override
    public Mono<ResponseEntity<Flux<TaskDto>>> getTasks(Integer page, Integer size, ServerWebExchange exchange) {
        var taskStream = taskService.findAll(page, size);
        return Mono.just(ResponseEntity.ok(taskStream));
    }

    //    @Operation(summary = "Retrieve a paginated list of tasks")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Successful retrieval of tasks",
//                    content = @Content(
//                            mediaType = "application/json",
//                            array = @ArraySchema(schema = @Schema(implementation = TaskDto.class))
//                    )
//            )
//    })
//    @GetMapping
//    public Mono<ResponseEntity<List<TaskDto>>> findAll(@RequestParam(defaultValue = "0") int page,
//                                                       @RequestParam(defaultValue = "10") int size) {
//        return taskService.findAll(page, size)
//                .collectList()
//                .map(ResponseEntity::ok);
//    }
//
//
//    @Operation(summary = "Retrieve a task by id")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Task retrieved successfully"),
//            @ApiResponse(responseCode = "404",
//                    description = "Task not found",
//                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))
//            ),
//    })
//    @GetMapping("/{id}")
//    public Mono<ResponseEntity<TaskDto>> findById(@PathVariable String id) {
//        log.info("Find task by id {}", id);
//        return taskService.findById(id)
//                .map(ResponseEntity::ok);
//    }
//
//    @Operation(summary = "Create a new task")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "201", description = "Task created successfully"),
//            @ApiResponse(responseCode = "400", description = "Bad request"),
//    })
//    @PostMapping
//    public Mono<ResponseEntity<TaskDto>> create(@RequestBody TaskCreateRequestDto taskCreationMono) {
//        return taskService.save(taskCreationMono)
//                .map(taskDto -> ResponseEntity.status(HttpStatus.CREATED).body(taskDto));
//    }
//
//    @Operation(summary = "Update a task")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Successful update of a task",
//                    content = @Content(
//                            mediaType = "application/json",
//                            schema = @Schema(implementation = TaskDto.class)
//                    )
//            )
//    })
//    @PutMapping("/{id}")
//    public Mono<ResponseEntity<TaskDto>> update(@PathVariable String id, @RequestBody TaskUpdateRequestDto taskUpdateRequestDto) {
//        log.info("Update task by id{} with data {}", id,  taskUpdateRequestDto);
//        return taskService.update(TaskUpdateDto.builder()
//                        .id(id)
//                        .taskUpdateRequestDto(taskUpdateRequestDto)
//                        .build())
//                .map(ResponseEntity::ok);
//    }
//
//    @Operation(summary = "Delete a task by its id")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
//    })
//    @DeleteMapping("/{id}")
//    public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
//        log.info("Delete task by id {}", id);
//        return taskService.delete(id)
//                .map(deletedTaskId -> ResponseEntity.noContent().build());
//    }

}

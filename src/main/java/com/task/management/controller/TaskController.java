package com.task.management.controller;


import com.task.management.dto.TaskRequestDto;
import com.task.management.dto.TaskDto;
import com.task.management.dto.TaskUpdateDto;
import com.task.management.service.TaskService;
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
import reactor.core.scheduler.Schedulers;


@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public Flux<TaskDto> findAll(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        log.info("Find all tasks with page {} and size {}", page, size);
        return Mono.fromCallable(() -> taskService.findAll(page, size))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<TaskDto>> findById(@PathVariable String id) {
        log.info("Find task by id {}", id);
        return Mono.fromCallable(() -> taskService.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

    @PostMapping
    public Mono<ResponseEntity<TaskDto>> save(@RequestBody Mono<TaskRequestDto> taskCreationMono) {
        return taskCreationMono
                .flatMap(this::persist)
                .map(taskDto -> ResponseEntity.status(HttpStatus.CREATED).body(taskDto));

    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<TaskDto>> update(@PathVariable String id, @RequestBody Mono<TaskRequestDto> taskRequestDtoMono) {
        log.info("Update task by id{} with data {}", id,  taskRequestDtoMono);
        return taskRequestDtoMono.flatMap(taskRequestDto ->
                        Mono.fromCallable(() -> taskService.update(TaskUpdateDto.builder()
                                        .id(id)
                                        .taskRequestDto(taskRequestDto)
                                        .build()))
                                .subscribeOn(Schedulers.boundedElastic()))
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<String>> delete(@PathVariable String id) {
        log.info("Delete task by id {}", id);
        return Mono.fromCallable(() -> taskService.delete(id))
                .map(deletedId -> ResponseEntity.status(HttpStatus.NO_CONTENT).body(deletedId))
                .subscribeOn(Schedulers.boundedElastic());
    }



    private Mono<TaskDto> persist(TaskRequestDto taskCreationRequestDto) {
        log.info("Persist task {}", taskCreationRequestDto);
        return Mono.fromCallable(() -> taskService.save(taskCreationRequestDto))
                .subscribeOn(Schedulers.boundedElastic());
    }

}

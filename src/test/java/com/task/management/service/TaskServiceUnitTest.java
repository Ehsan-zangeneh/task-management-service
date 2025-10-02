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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;


@ExtendWith(MockitoExtension.class)
public class TaskServiceUnitTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    @DisplayName("should call taskRepository.findById by the given taskId")
    void findById_should_succeed() {

        // given
        var taskId = UUID.randomUUID();
        var title = "title";
        var description = "description";
        var task = Task.builder()
                .id(taskId)
                .title(title)
                .description(description)
                .status(TaskStatus.APPROVED)
                .build();
        Mockito.when(taskRepository.findById(taskId))
                .thenReturn(Mono.just(task));

        //when
        var result = taskService.findById(taskId);

        //then
        StepVerifier.create(result)
                .expectNextMatches(taskDto -> taskDto.getClass().equals(TaskDto.class) &&
                        taskDto.getId().equals(taskId) &&
                        taskDto.getTitle().equals(title) &&
                        taskDto.getDescription().equals(description) &&
                        taskDto.getStatus().equals(com.task.management.dto.TaskStatus.APPROVED)
                )
                .verifyComplete();

    }

    @Test
    @DisplayName("should throw TaskNotFoundException")
    void findById_throw_Exception() {

        // given
        var taskId = UUID.randomUUID();
        Mockito.when(taskRepository.findById(taskId))
                .thenReturn(Mono.empty());

        //when
        var result = taskService.findById(taskId);

        //then
        StepVerifier.create(result)
                .expectError(TaskNotFoundException.class)
                .verify();

    }

    @Test
    @DisplayName("should return multiple tasks")
    void findAll_should_succeed() {

        // given
        int page = 1;
        int size = 2;
        var taskId1 = UUID.randomUUID();
        var title1 = "title1";
        var description1 = "description1";
        var task1 = Task.builder()
                .id(taskId1)
                .title(title1)
                .description(description1)
                .status(TaskStatus.APPROVED)
                .build();

        //and
        var taskId2 = UUID.randomUUID();
        var title2 = "title2";
        var description2 = "description2";
        var task2 = Task.builder()
                .id(taskId2)
                .title(title2)
                .description(description2)
                .status(TaskStatus.APPROVED)
                .build();

        //and
        Mockito.when(taskRepository.findAllPaged(size, page * size))
                .thenReturn(Flux.just(task1, task2));

        //when
        var result = taskService.findAll(page, size);

        //then
        StepVerifier.create(result)
                .expectNextMatches(taskDto -> taskDto.getId().equals(taskId1))
                .expectNextMatches(taskDto -> taskDto.getId().equals(taskId2))
                .verifyComplete();

    }

    @Test
    @DisplayName("should return an empty collection")
    void findAll_should_succeed_with_empty_list() {

        // given
        int page = 1;
        int size = 2;

        //and
        Mockito.when(taskRepository.findAllPaged(size, page * size))
                .thenReturn(Flux.empty());

        //when
        var result = taskService.findAll(page, size);

        //then
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();

    }

    @Test
    @DisplayName("should call save method from repository")
    void save_succeed() {

        //given
        var assigneeId = UUID.randomUUID();
        var ownerId = UUID.randomUUID();
        var title = "title";
        var description = "description";
        var taskCreateRequestDto = TaskCreateRequestDto.builder()
                .title(title)
                .description(description)
                .assigneeId(assigneeId)
                .ownerId(ownerId)
                .build();

        //and mock the returning value from save method
        Mockito.when(taskRepository.save(Mockito.any(Task.class)))
                .thenReturn(Mono.just(Task.builder()
                        .id(UUID.randomUUID())
                        .title(title)
                        .description(description)
                        .assigneeId(assigneeId)
                        .ownerId(ownerId)
                        .status(TaskStatus.TODO)
                        .build()));

        //when
        taskService.save(taskCreateRequestDto).block();

        //then
        Mockito.verify(taskRepository).save(Mockito.argThat(
                task -> task.getAssigneeId().equals(assigneeId) &&
                        task.getOwnerId().equals(ownerId) &&
                        task.getTitle().equals(title) &&
                        task.getDescription().equals(description) &&
                        task.getStatus().equals(TaskStatus.TODO) &&
                        Objects.nonNull(task.getCreationDate())
        ));
    }

    @Test
    @DisplayName("should attempt to update the task")
    void update_succeed() {

        //given
        var assigneeId = UUID.randomUUID();
        var ownerId = UUID.randomUUID();
        var title = "title";
        var description = "description";
        var taskUpdateRequestDto= TaskUpdateRequestDto.builder()
                .title(title)
                .description(description)
                .assigneeId(assigneeId)
                .build();
        var taskId = UUID.randomUUID();

        //and mock the returning value from findById method from repository
        var task = Task.builder()
                .id(taskId)
                .title(title)
                .description(description)
                .status(TaskStatus.APPROVED)
                .ownerId(ownerId)
                .assigneeId(assigneeId)
                .creationDate(ZonedDateTime.now())
                .build();
        Mockito.when(taskRepository.findById(taskId))
                .thenReturn(Mono.just(task));

        //and mock the returning value from save method from repository
        Mockito.when(taskRepository.save(Mockito.any(Task.class)))
                .thenReturn(Mono.just(task));

        //when
        taskService.update(TaskUpdateDto.builder()
                        .taskUpdateRequestDto(taskUpdateRequestDto)
                        .id(taskId)
                        .build())
                .block();

        //then
        Mockito.verify(taskRepository).save(Mockito.argThat(
                taskDto -> taskDto.getAssigneeId().equals(assigneeId) &&
                        taskDto.getOwnerId().equals(ownerId) &&
                        taskDto.getTitle().equals(title) &&
                        taskDto.getDescription().equals(description) &&
                        taskDto.getStatus().equals(TaskStatus.APPROVED) &&
                        Objects.nonNull(taskDto.getCreationDate())
        ));
    }

    @Test
    @DisplayName("should throw TaskNotFoundException")
    void update_throws_Exception() {

        //given
        var assigneeId = UUID.randomUUID();
        var ownerId = UUID.randomUUID();
        var title = "title";
        var description = "description";
        var taskUpdateRequestDto= TaskUpdateRequestDto.builder()
                .title(title)
                .description(description)
                .assigneeId(assigneeId)
                .build();
        var taskId = UUID.randomUUID();

        //and mock the returning value from findById method from repository
        Mockito.when(taskRepository.findById(taskId))
                .thenReturn(Mono.empty());

        //when
        var result = taskService.update(TaskUpdateDto.builder()
                .taskUpdateRequestDto(taskUpdateRequestDto)
                .id(taskId)
                .build());

        //then
        StepVerifier.create(result)
                .expectError(TaskNotFoundException.class)
                .verify();

    }

    @ParameterizedTest
    @EnumSource(value = TaskStatus.class, names = {"TODO", "CANCELLED"})
    @DisplayName("should call deleteById method from repository for valid statuses")
    void delete_succeed(TaskStatus status) {

        //given
        var assigneeId = UUID.randomUUID();
        var ownerId = UUID.randomUUID();
        var title = "title";
        var description = "description";
        var taskId = UUID.randomUUID();
        var task = Task.builder()
                .id(taskId)
                .title(title)
                .description(description)
                .status(status)
                .ownerId(ownerId)
                .assigneeId(assigneeId)
                .creationDate(ZonedDateTime.now())
                .build();

        //and mock the returning value from findById method from repository
        Mockito.when(taskRepository.findById(taskId))
                .thenReturn(Mono.just(task));
        Mockito.when(taskRepository.deleteById(taskId))
                .thenReturn(Mono.empty());

        //when
        taskService.delete(taskId).block();

        //then
        Mockito.verify(taskRepository).deleteById(taskId);

    }

    @ParameterizedTest
    @EnumSource(value = TaskStatus.class, names = {"APPROVED", "DONE", "IN_PROGRESS", "UNDER_REVIEW"})
    @DisplayName("should throw IllegalTaskManagementOperationException for invalid statuses")
    void delete_throw_exception(TaskStatus status) {

        //given
        var assigneeId = UUID.randomUUID();
        var ownerId = UUID.randomUUID();
        var title = "title";
        var description = "description";
        var taskId = UUID.randomUUID();
        var task = Task.builder()
                .id(taskId)
                .title(title)
                .description(description)
                .status(status)
                .ownerId(ownerId)
                .assigneeId(assigneeId)
                .creationDate(ZonedDateTime.now())
                .build();

        //and mock the returning value from findById method from repository
        Mockito.when(taskRepository.findById(taskId))
                .thenReturn(Mono.just(task));

        //when
        var result = taskService.delete(taskId);

        //then
        StepVerifier.create(result)
                .expectError(IllegalTaskManagementOperationException.class)
                .verify();

    }

    @Test
    @DisplayName("should throw exception for non-existing task")
    void delete_throw_notfound_exception() {

        //given
        var taskId = UUID.randomUUID();
        Mockito.when(taskRepository.findById(taskId))
                .thenReturn(Mono.empty());

        //when
        var result = taskService.delete(taskId);

        //then
        StepVerifier.create(result)
                .expectError(TaskNotFoundException.class)
                .verify();

    }



}

package com.task.management.controller;

import com.task.management.common.IntegrationTest;
import com.task.management.dto.TaskCreateRequestDto;
import com.task.management.dto.TaskDto;
import com.task.management.dto.TaskUpdateRequestDto;
import com.task.management.model.Task;
import com.task.management.model.TaskStatus;
import com.task.management.repository.TaskRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureWebTestClient
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class TaskControllerIntegrationTest extends IntegrationTest {


    @Autowired
    private TaskRepository taskRepository;

    @Nested
    @DisplayName("Test Get /tasks")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetAllTest {
        static List<Task> tasksInDatabase = List.of(
                Task.builder()
                        .title("Task 1")
                        .description("Description 1")
                        .status(TaskStatus.DONE)
                        .build(),
                Task.builder()
                        .title("Task 2")
                        .description("Description 2")
                        .status(TaskStatus.IN_PROGRESS)
                        .build(),
                Task.builder()
                        .title("Task 3")
                        .description("Description 3")
                        .status(TaskStatus.UNDER_REVIEW)
                        .build(),
                Task.builder()
                        .title("Task 4")
                        .description("Description 4")
                        .status(TaskStatus.TODO)
                        .build(),
                Task.builder()
                        .title("Task 5")
                        .description("Description 5")
                        .status(TaskStatus.CANCELLED)
                        .build(),
                Task.builder()
                        .title("Task 6")
                        .description("Description 6")
                        .status(TaskStatus.APPROVED)
                        .build()
        );


        @BeforeAll
        void setupAll() {
            taskRepository.deleteAll().block();
            taskRepository.saveAll(tasksInDatabase).collectList().block();
        }

        @Test
        @DisplayName("should return second page of size 3")
        @Order(1)
        void test_getAll_succeed() {

            //given
            var page = 1;
            var size = 3;

            //when
            var response = webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/tasks")
                            .queryParam("page", page)
                            .queryParam("size", size)
                            .build())
                    .exchange();

            //then
            response.expectStatus().isOk()
                    .expectBodyList(TaskDto.class)
                    .hasSize(size)
                    .consumeWith(result -> {
                        List<TaskDto> taskList = result.getResponseBody();
                        assert taskList != null;
                        assert taskList.stream().anyMatch(t -> t.getTitle().equals("Task 4"));
                        assert taskList.stream().anyMatch(t -> t.getTitle().equals("Task 5"));
                        assert taskList.stream().anyMatch(t -> t.getTitle().equals("Task 6"));
                    });

        }

        @Test
        @DisplayName("should call with default parameters")
        @Order(2)
        void test_getAll_default() {

            //given
            var size = tasksInDatabase.size();

            //when
            var response = webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/tasks")
                            .build())
                    .exchange();

            //then
            response.expectStatus().isOk()
                    .expectBodyList(TaskDto.class)
                    .hasSize(size)
                    .consumeWith(result -> {
                        List<TaskDto> taskList = result.getResponseBody();
                        assert taskList != null;
                        assert taskList.stream().anyMatch(t -> t.getTitle().equals("Task 1"));
                        assert taskList.stream().anyMatch(t -> t.getTitle().equals("Task 2"));
                        assert taskList.stream().anyMatch(t -> t.getTitle().equals("Task 3"));
                        assert taskList.stream().anyMatch(t -> t.getTitle().equals("Task 4"));
                        assert taskList.stream().anyMatch(t -> t.getTitle().equals("Task 5"));
                        assert taskList.stream().anyMatch(t -> t.getTitle().equals("Task 6"));
                    });

        }

        @ParameterizedTest(name = "Invalid params: page={0}, size={1}")
        @CsvSource({
                "-1, 3",   // page is negative
                "0, -3",   // size is negative
                "-1, -3"   // both are negative
        })
        @DisplayName("should throw exception for illegal parameter")
        @Order(3)
        void test_getAll_validation_error(int page, int size) {

            //when
            var response = webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/tasks")
                            .queryParam("page", page)
                            .queryParam("size", size)
                            .build())
                    .exchange();

            //then
            response.expectStatus().isBadRequest()
                    .expectBody(String.class)
                    .consumeWith(result -> {
                        String errorMessage = result.getResponseBody();
                        assert errorMessage != null && errorMessage.contains("must be greater than or equal to 0");
                    });

        }

        @Test
        @DisplayName("should return an empty list")
        @Order(Integer.MAX_VALUE)
        void test_getAll_empty() {

            //given
            taskRepository.deleteAll().block();

            //when
            var response = webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/tasks")
                            .build())
                    .exchange();

            //then
            response.expectStatus().isOk()
                    .expectBodyList(TaskDto.class)
                    .hasSize(0)
                    .consumeWith(result -> {
                        List<TaskDto> taskList = result.getResponseBody();
                        assert taskList != null;
                    });

        }
}

    @Nested
    @DisplayName("Test Get /tasks/{id}")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class GetByIdTest {

        @BeforeEach
        void setup() {
            taskRepository.deleteAll().block();
        }

        @Test
        @DisplayName("should return a task")
        void test_getById_succeed() {
            //given
            Task task = Task.builder()
                    .title("Task 1")
                    .description("description")
                    .status(TaskStatus.DONE)
                    .build();
            var taskId = Objects.requireNonNull(taskRepository.save(task).block())
                    .getId();

            //when
            var response = webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/tasks/{id}")
                            .build(taskId.toString()))
                    .exchange();

            //then
            response.expectStatus().isOk()
                    .expectBody(TaskDto.class)
                    .consumeWith(result -> {
                        TaskDto taskDto = result.getResponseBody();
                        assert taskDto != null;
                        assert taskDto.getTitle().equals("Task 1");
                        assert taskDto.getDescription().equals("description");
                        assert taskDto.getStatus().name().equals(TaskStatus.DONE.name());
                    });

        }

        @Test
        @DisplayName("should throw TaskNotFoundException")
        void test_getById_throw_not_found() {
            //given
            Task task = Task.builder()
                    .title("Task 1")
                    .description("description")
                    .status(TaskStatus.DONE)
                    .build();
            taskRepository.save(task).block();
            var nonExistingTaskId = UUID.randomUUID();

            //when
            var response = webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/tasks/{id}")
                            .build(nonExistingTaskId.toString()))
                    .exchange();

            //then
            response.expectStatus().isNotFound()
                    .expectBody(String.class)
                    .consumeWith(result -> {
                        String errorResponse = result.getResponseBody();
                        assert errorResponse != null &&
                                errorResponse.contains("not found with id:{%s}".formatted(nonExistingTaskId.toString()));
                    });

        }

        @Test
        @DisplayName("should throw TaskNotFoundException")
        void test_getById_validation_error() {
            //given
            var invalidTaskId = "invalidTaskId";
            //when
            var response = webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/tasks/{id}")
                            .build(invalidTaskId))
                    .exchange();

            //then
            response.expectStatus().isBadRequest()
                    .expectBody(String.class)
                    .consumeWith(result -> {
                        String errorResponse = result.getResponseBody();
                        assert errorResponse != null &&
                                errorResponse.contains("Type mismatch.");
                    });

        }


    }

    @Nested
    @DisplayName("Test post /tasks")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class PostTest {

        @BeforeEach
        void setup() {
            taskRepository.deleteAll().block();
        }

        @Test
        @DisplayName("should create a task with status 'TODO'")
        void post_succeed() {

            //given
            var ownerId = UUID.randomUUID();
            var assigneeId = UUID.randomUUID();
            var taskCreateRequest = TaskCreateRequestDto.builder()
                    .description("description")
                    .title("title")
                    .ownerId(ownerId)
                    .assigneeId(assigneeId)
                    .build();

            //when
            var response = webTestClient.post()
                    .uri("/tasks")
                    .body(Mono.just(taskCreateRequest), TaskCreateRequestDto.class)
                    .exchange();

            //then
            response.expectStatus().isCreated()
                    .expectBody(TaskDto.class)
                    .consumeWith(result -> {
                        TaskDto taskDto = result.getResponseBody();
                        assert taskDto != null;
                        assert taskDto.getTitle().equals("title");
                        assert taskDto.getDescription().equals("description");
                        assert taskDto.getOwnerId().equals(ownerId);
                        assert taskDto.getAssigneeId().equals(assigneeId);
                        assert taskDto.getStatus().name().equals(TaskStatus.TODO.name());
                        assert taskDto.getCreationDate() != null;
                    });

        }

        @Test
        @DisplayName("should throw exception")
        void post_throw_exception() {

            //given
            var invalidRequestBody = Mono.empty();

            //when
            var response = webTestClient.post()
                    .uri("/tasks")
                    .body(invalidRequestBody, TaskCreateRequestDto.class)
                    .exchange();

            //then
            response.expectStatus().isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                    .expectBody(String.class)
                    .consumeWith(result -> {
                        String errorResponse = result.getResponseBody();
                        assert errorResponse != null;
                        assert errorResponse.contains("not supported");
                    });

        }

        @ParameterizedTest(name = "Invalid params: title={0}, ownerId={1}, errorMessage={2}")
        @MethodSource("argumentProvider")
        @DisplayName("should throw validation exception")
        void post_validation_error(String title, UUID ownerId, String errorMessage) {

            //given
            var assigneeId = UUID.randomUUID();
            var taskCreateRequest = TaskCreateRequestDto.builder()
                    .description("description")
                    .title(title)
                    .ownerId(ownerId)
                    .assigneeId(assigneeId)
                    .build();

            //when
            var response = webTestClient.post()
                    .uri("/tasks")
                    .body(Mono.just(taskCreateRequest), TaskCreateRequestDto.class)
                    .exchange();

            //then
            response.expectStatus().isBadRequest()
                    .expectBody(String.class)
                    .consumeWith(result -> {
                        String errorResponse  = result.getResponseBody();
                        assert errorResponse != null;
                        assert errorResponse.contains(errorMessage);
                    });

        }

        public Stream<Arguments> argumentProvider() {

            return Stream.of(
                    Arguments.of("title", null, "[Field 'ownerId': must not be null]"),
                    Arguments.of(null, UUID.randomUUID(),  "[Field 'title': must not be null]"),
                    Arguments.of("", UUID.randomUUID(), "size must be between 1 and 2147483647")
            );
        }

    }

    @Nested
    @DisplayName("Test put /tasks/{id}")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class PutTest {

        @BeforeEach
        void setup() {
            taskRepository.deleteAll().block();
        }

        @ParameterizedTest(name = "Illegal params: updateTitle={0}, taskAssigneeId={1}, taskStatus={2}, updatedDescription{3}")
        @MethodSource("taskUpdateValidArgumentProvider")
        @DisplayName("should update a task by id")
        void put_succeed(String updateTitle,
                         String updatedDescription,
                         UUID taskAssigneeId,
                         com.task.management.dto.TaskStatus taskStatus) {

            //given
            var task = taskRepository.save(Task.builder()
                    .title("title")
                    .description("description")
                    .ownerId(UUID.randomUUID())
                    .creationDate(ZonedDateTime.now())
                    .status(TaskStatus.TODO)
                    .build()).block();

            //and
            var updateRequestDto = TaskUpdateRequestDto.builder()
                    .title(updateTitle)
                    .description(updatedDescription)
                    .assigneeId(taskAssigneeId)
                    .status(taskStatus)
                    .build();
            var existingTaskId = task.getId();
            var title = updateTitle !=  null ? updateTitle : task.getTitle();
            var description = updatedDescription !=  null ? updatedDescription : task.getDescription();
            var ownerId = task.getOwnerId();
            var status = taskStatus != null ? taskStatus : task.getStatus();
            var assigneeId = taskAssigneeId != null ? taskAssigneeId : task.getAssigneeId();

            //when
            var response = webTestClient.put()
                    .uri(uriBuilder -> uriBuilder
                            .path("/tasks/{id}")
                            .build(existingTaskId))
                    .body(Mono.just(updateRequestDto), TaskCreateRequestDto.class)
                    .exchange();

            //then
            response.expectStatus().isOk()
                    .expectBody(TaskDto.class)
                    .consumeWith(result -> {
                        TaskDto taskDto = result.getResponseBody();
                        assert taskDto != null;
                        assert taskDto.getTitle().equals(title);
                        assert taskDto.getDescription().equals(description);
                        assert taskDto.getOwnerId().equals(ownerId);
                        assert taskDto.getAssigneeId().equals(assigneeId);
                        assert taskDto.getStatus().name().equals(status.name());
                        assert taskDto.getCreationDate() != null;
                        assert taskDto.getModificationDate() != null;
                    });

        }

        @ParameterizedTest(name = "Illegal params: taskIdForUpdate={0}, updateTitle={1}, assigneeId={2}, taskStatus={3}")
        @MethodSource("taskUpdateInvalidArgumentProvider")
        @DisplayName("should throw exception")
        void put_throw_exception(UUID taskIdForUpdate,
                                 String updateTitle,
                                 UUID assigneeId,
                                 com.task.management.dto.TaskStatus taskStatus,
                                 HttpStatus httpStatus,
                                 String errorMessage) {

            //given
            var ownerId = UUID.randomUUID();
            var task = taskRepository.save(Task.builder()
                    .title("title")
                    .description("description")
                    .ownerId(ownerId)
                    .creationDate(ZonedDateTime.now())
                    .status(TaskStatus.TODO)
                    .build()).block();

            var taskId = taskIdForUpdate != null ? taskIdForUpdate : task.getId();

            //and
            var updateRequestDto = TaskUpdateRequestDto.builder()
                    .title(updateTitle)
                    .assigneeId(assigneeId)
                    .status(taskStatus)
                    .build();

            //when
            var response = webTestClient.put()
                    .uri(uriBuilder -> uriBuilder
                            .path("/tasks/{id}")
                            .build(taskId))
                    .body(Mono.just(updateRequestDto), TaskCreateRequestDto.class)
                    .exchange();

            //then
            response.expectStatus().isEqualTo(httpStatus)
                    .expectBody(String.class)
                    .consumeWith(result -> {
                        String errorResponse = result.getResponseBody();
                        assert errorResponse != null;
                        assert errorResponse.contains(errorMessage);
                    });

        }

//        @Test
//        @DisplayName("should throw exception because of illegal operation")
//        void put_illegal_operation_error() {
//
//            //give
//            var task = taskRepository.save(Task.builder()
//                    .title("title")
//                    .description("description")
//                    .ownerId(ownerId)
//                    .creationDate(ZonedDateTime.now())
//                    .status(TaskStatus.TODO)
//                    .build()).block();
//        }

//        @ParameterizedTest(name = "Invalid params: title={0}, ownerId={1}, errorMessage={2}")
//        @MethodSource("")
//        @DisplayName("should throw validation exception")
//        void put_validation_error(String title, UUID ownerId, String errorMessage) {
//
//
//        }
public Stream<Arguments> taskUpdateValidArgumentProvider() {
    return Stream.of(
            Arguments.of("task title",
                    "New Description",
                    UUID.randomUUID(),
                    com.task.management.dto.TaskStatus.IN_PROGRESS),
            Arguments.of("task title",
                    "New Description",
                    UUID.randomUUID(),
                    com.task.management.dto.TaskStatus.DONE),
            Arguments.of("task title",
                    "New Description",
                    UUID.randomUUID(),
                    com.task.management.dto.TaskStatus.APPROVED,
                    HttpStatus.BAD_REQUEST,
                    "must not be blank"),
            Arguments.of("task title",
                    "New Description",
                    UUID.randomUUID(),
                    com.task.management.dto.TaskStatus.UNDER_REVIEW)
    );
}

        public Stream<Arguments> taskUpdateInvalidArgumentProvider() {
            return Stream.of(
                    Arguments.of(UUID.randomUUID(),
                            "task title",
                            UUID.randomUUID(),
                            com.task.management.dto.TaskStatus.IN_PROGRESS,
                            HttpStatus.NOT_FOUND,
                            "Task not found"),
                    Arguments.of(null,
                            "",
                            UUID.randomUUID(),
                            com.task.management.dto.TaskStatus.IN_PROGRESS,
                            HttpStatus.BAD_REQUEST,
                            "must not be blank"),
                    Arguments.of(null,
                            "    ",
                            UUID.randomUUID(),
                            com.task.management.dto.TaskStatus.IN_PROGRESS,
                            HttpStatus.BAD_REQUEST,
                            "must not be blank"),
                    Arguments.of(null,
                            "valid title",
                            null,
                            com.task.management.dto.TaskStatus.IN_PROGRESS,
                            HttpStatus.BAD_REQUEST,
                            "not valid for update")
            );
        }

    }
}
package com.task.management.controller;

import com.task.management.common.IntegrationTest;
import com.task.management.dto.TaskDto;
import com.task.management.model.Task;
import com.task.management.model.TaskStatus;
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
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.print.DocFlavor;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureWebTestClient
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class TaskControllerIntegrationTest extends IntegrationTest {

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


}
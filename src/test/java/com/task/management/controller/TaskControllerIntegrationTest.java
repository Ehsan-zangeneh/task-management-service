package com.task.management.controller;

import com.task.management.dto.TaskDto;
import com.task.management.model.Task;
import com.task.management.model.TaskStatus;
import com.task.management.repository.TaskRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureWebTestClient
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class TaskControllerIntegrationTest {

    static DockerImageName myImage = DockerImageName.parse("docker.arvancloud.ir/postgres").asCompatibleSubstituteFor("postgres");

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(myImage)
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        postgres.start();
        registry.add("spring.r2dbc.url", () ->
                "r2dbc:postgresql://" + postgres.getHost() + ":" + postgres.getMappedPort(5432) + "/" + postgres.getDatabaseName());
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);

        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
    }

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private TaskRepository taskRepository;

    @Nested
    @DisplayName("Test Get /tasks")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetAllTasks {

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
        void test_getAll_throw_exception(int page, int size) {

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

}
package com.task.management.repository;

import com.task.management.model.Task;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface TaskRepository extends ReactiveCrudRepository<Task, String> {

    @Query("SELECT * FROM task LIMIT :limit OFFSET :offset")
    Flux<Task> findAllPaged(@Param("limit") int limit, @Param("offset") int offset);

}

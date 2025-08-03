package com.railse.hiring.workforcemgmt.repository;

import com.railse.hiring.workforcemgmt.model.TaskComment;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryTaskCommentRepository implements TaskCommentRepository {

    private final Map<Long, TaskComment> commentStore = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    @Override
    public TaskComment save(TaskComment comment) {
        comment.setId(idCounter.incrementAndGet());
        commentStore.put(comment.getId(), comment);
        return comment;
    }

    @Override
    public List<TaskComment> findByTaskId(Long taskId) {
        return commentStore.values().stream()
                .filter(comment -> comment.getTaskId().equals(taskId))
                .sorted(Comparator.comparing(TaskComment::getTimestamp))
                .collect(Collectors.toList());
    }
}

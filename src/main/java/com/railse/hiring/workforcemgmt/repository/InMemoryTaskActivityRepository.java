package com.railse.hiring.workforcemgmt.repository;

import com.railse.hiring.workforcemgmt.model.TaskActivity;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryTaskActivityRepository implements TaskActivityRepository {

    private final Map<Long, TaskActivity> activityStore = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    @Override
    public TaskActivity save(TaskActivity activity) {
        activity.setId(idCounter.incrementAndGet());
        activityStore.put(activity.getId(), activity);
        return activity;
    }

    @Override
    public List<TaskActivity> findByTaskId(Long taskId) {
        return activityStore.values().stream()
                .filter(activity -> activity.getTaskId().equals(taskId))
                .sorted(Comparator.comparing(TaskActivity::getTimestamp))
                .collect(Collectors.toList());
    }
}

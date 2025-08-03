package com.railse.hiring.workforcemgmt.service;

import com.railse.hiring.workforcemgmt.dto.*;
import com.railse.hiring.workforcemgmt.model.enums.TaskPriority;

import com.railse.hiring.workforcemgmt.dto.TaskManagementDto;
import com.railse.hiring.workforcemgmt.model.TaskComment;

import java.util.List;

public interface TaskManagementService {
    List<TaskManagementDto> createTasks(TaskCreateRequest request);
    List<TaskManagementDto> updateTasks(UpdateTaskRequest request);
    String assignByReference(AssignByReferenceRequest request);
    List<TaskManagementDto> fetchTasksByDate(TaskFetchByDateRequest request);
    TaskManagementDto findTaskById(Long id);
    String updatePriority(Long taskId, TaskPriority priority);
    List<TaskManagementDto> getTasksByPriority(TaskPriority priority);
    void addComment(Long taskId, Long userId, String comment);
    List<TaskComment> getCommentsForTask(Long taskId);
    List<String> getActivityHistoryForTask(Long taskId);
    TaskManagementDto getTaskWithHistory(Long taskId);
}

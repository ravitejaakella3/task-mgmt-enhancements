package com.railse.hiring.workforcemgmt.service.impl;

import com.railse.hiring.workforcemgmt.common.exception.ResourceNotFoundException;
import com.railse.hiring.workforcemgmt.dto.*;
import com.railse.hiring.workforcemgmt.mapper.ITaskManagementMapper;
import com.railse.hiring.workforcemgmt.model.TaskManagement;
import com.railse.hiring.workforcemgmt.model.enums.Task;
import com.railse.hiring.workforcemgmt.model.enums.TaskStatus;
import com.railse.hiring.workforcemgmt.repository.TaskRepository;
import com.railse.hiring.workforcemgmt.service.TaskManagementService;
import org.springframework.stereotype.Service;
import com.railse.hiring.workforcemgmt.model.enums.TaskPriority;
import com.railse.hiring.workforcemgmt.model.TaskComment;
import com.railse.hiring.workforcemgmt.model.TaskActivity;
import com.railse.hiring.workforcemgmt.repository.TaskCommentRepository;
import com.railse.hiring.workforcemgmt.repository.TaskActivityRepository;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskManagementServiceImpl implements TaskManagementService {

    private final TaskCommentRepository taskCommentRepository;
    private final TaskActivityRepository taskActivityRepository;
    private final TaskRepository taskRepository;
    private final ITaskManagementMapper taskMapper;

    public TaskManagementServiceImpl(
        TaskRepository taskRepository,
        ITaskManagementMapper taskMapper,
        TaskCommentRepository taskCommentRepository,
        TaskActivityRepository taskActivityRepository
    ) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.taskCommentRepository = taskCommentRepository;
        this.taskActivityRepository = taskActivityRepository;
    }

    @Override
    public void addComment(Long taskId, Long userId, String comment) {
        TaskComment taskComment = new TaskComment();
        taskComment.setTaskId(taskId);
        taskComment.setUserId(userId);
        taskComment.setComment(comment);
        taskComment.setTimestamp(System.currentTimeMillis());
        taskCommentRepository.save(taskComment);

        TaskActivity activity = new TaskActivity();
        activity.setTaskId(taskId);
        activity.setMessage("User " + userId + " added comment: " + comment);
        activity.setTimestamp(System.currentTimeMillis());
        taskActivityRepository.save(activity);
    }

    @Override
    public List<TaskComment> getCommentsForTask(Long taskId) {
        return taskCommentRepository.findByTaskId(taskId);
    }

    @Override
    public List<String> getActivityHistoryForTask(Long taskId) {
        return taskActivityRepository.findByTaskId(taskId).stream()
                .sorted((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()))
                .map(TaskActivity::getMessage)
                .collect(Collectors.toList());
    }

    @Override
    public TaskManagementDto getTaskWithHistory(Long taskId) {
        TaskManagement task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        TaskManagementDto dto = taskMapper.modelToDto(task);
        dto.setComments(getCommentsForTask(taskId)); // Make sure DTO has this field
        dto.setActivityLogs(getActivityHistoryForTask(taskId));
        return dto;
    }

    @Override
    public TaskManagementDto findTaskById(Long id) {
        TaskManagement task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return taskMapper.modelToDto(task);
    }

    @Override
    public List<TaskManagementDto> createTasks(TaskCreateRequest createRequest) {
        List<TaskManagement> createdTasks = new ArrayList<>();
        for (TaskCreateRequest.RequestItem item : createRequest.getRequests()) {
            TaskManagement newTask = new TaskManagement();
            newTask.setReferenceId(item.getReferenceId());
            newTask.setReferenceType(item.getReferenceType());
            newTask.setTask(item.getTask());
            newTask.setAssigneeId(item.getAssigneeId());
            newTask.setPriority(item.getPriority());
            newTask.setTaskDeadlineTime(item.getTaskDeadlineTime());
            newTask.setStatus(TaskStatus.ASSIGNED);
            newTask.setDescription("New task created.");
            newTask.setCreatedAt(System.currentTimeMillis());
            createdTasks.add(taskRepository.save(newTask));
        }
        return taskMapper.modelListToDtoList(createdTasks);
    }

    @Override
    public List<TaskManagementDto> updateTasks(UpdateTaskRequest updateRequest) {
        List<TaskManagement> updatedTasks = new ArrayList<>();
        for (UpdateTaskRequest.RequestItem item : updateRequest.getRequests()) {
            TaskManagement task = taskRepository.findById(item.getTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + item.getTaskId()));

            if (item.getTaskStatus() != null) {
                task.setStatus(item.getTaskStatus());
            }
            if (item.getDescription() != null) {
                task.setDescription(item.getDescription());
            }
            updatedTasks.add(taskRepository.save(task));
        }
        return taskMapper.modelListToDtoList(updatedTasks);
    }

    @Override
    public String assignByReference(AssignByReferenceRequest request) {
        List<Task> applicableTasks = Task.getTasksByReferenceType(request.getReferenceType());

        List<TaskManagement> existingTasks = taskRepository.findByReferenceIdAndReferenceType(
                request.getReferenceId(), request.getReferenceType());

        for (Task taskType : applicableTasks) {
            for (TaskManagement existingTask : existingTasks) {
                if (existingTask.getTask() == taskType &&
                    existingTask.getStatus() != TaskStatus.COMPLETED &&
                    existingTask.getStatus() != TaskStatus.CANCELLED) {

                    existingTask.setStatus(TaskStatus.CANCELLED);
                    existingTask.setDescription("Cancelled due to reassignment");
                    taskRepository.save(existingTask);
                }
            }

            TaskManagement newTask = new TaskManagement();
            newTask.setReferenceId(request.getReferenceId());
            newTask.setReferenceType(request.getReferenceType());
            newTask.setTask(taskType);
            newTask.setAssigneeId(request.getAssigneeId());
            newTask.setStatus(TaskStatus.ASSIGNED);
            newTask.setDescription("New task assigned via assignByReference");

            taskRepository.save(newTask);
        }

        return "Tasks reassigned successfully for reference " + request.getReferenceId();
    }


    @Override
    public List<TaskManagementDto> fetchTasksByDate(TaskFetchByDateRequest request) {
        List<TaskManagement> tasks = taskRepository.findByAssigneeIdIn(request.getAssigneeIds());

        List<TaskManagement> filteredTasks = tasks.stream()
                .filter(task -> {
                    Long createdAt = task.getCreatedAt();
                    boolean isOpen = task.getStatus() != TaskStatus.CANCELLED &&
                                    task.getStatus() != TaskStatus.COMPLETED;
                    boolean inDateRange = createdAt != null &&
                                        createdAt >= request.getStartDate() &&
                                        createdAt <= request.getEndDate() &&
                                        isOpen;
                    boolean beforeStartButStillOpen = createdAt != null &&
                                                    createdAt < request.getStartDate() &&
                                                    isOpen;

                    return inDateRange || beforeStartButStillOpen;
                })
                .collect(Collectors.toList());

        return taskMapper.modelListToDtoList(filteredTasks);
    }




    @Override
    public String updatePriority(Long taskId, TaskPriority priority) {
        TaskManagement task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setPriority(priority);
        taskRepository.save(task);
        return "Task priority updated to " + priority;
    }

    @Override
    public List<TaskManagementDto> getTasksByPriority(TaskPriority priority) {
        List<TaskManagement> tasks = taskRepository.findByPriority(priority);
        return taskMapper.modelListToDtoList(tasks);
    }


}

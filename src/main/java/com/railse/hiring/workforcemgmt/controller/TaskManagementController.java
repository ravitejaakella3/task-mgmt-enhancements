package com.railse.hiring.workforcemgmt.controller;

import com.railse.hiring.workforcemgmt.common.model.response.Response;
import com.railse.hiring.workforcemgmt.dto.*;
import com.railse.hiring.workforcemgmt.model.enums.TaskPriority;
import com.railse.hiring.workforcemgmt.service.TaskManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task-mgmt")
public class TaskManagementController {

    private final TaskManagementService taskManagementService;

    public TaskManagementController(TaskManagementService taskManagementService) {
        this.taskManagementService = taskManagementService;
    }

    @GetMapping("/{id}")
    public Response<TaskManagementDto> getTaskById(@PathVariable Long id) {
        return new Response<>(taskManagementService.findTaskById(id));
    }

    @PostMapping("/create")
    public Response<List<TaskManagementDto>> createTasks(@RequestBody TaskCreateRequest request) {
        return new Response<>(taskManagementService.createTasks(request));
    }

    @PostMapping("/update")
    public Response<List<TaskManagementDto>> updateTasks(@RequestBody UpdateTaskRequest request) {
        return new Response<>(taskManagementService.updateTasks(request));
    }

    @PostMapping("/assign-by-ref")
    public Response<String> assignByReference(@RequestBody AssignByReferenceRequest request) {
        return new Response<>(taskManagementService.assignByReference(request));
    }

    @PostMapping("/fetch-by-date/v2")
    public Response<List<TaskManagementDto>> fetchByDate(@RequestBody TaskFetchByDateRequest request) {
        return new Response<>(taskManagementService.fetchTasksByDate(request));
    }

    @PutMapping("/priority/{taskId}")
    public ResponseEntity<Response<String>> updatePriority(
            @PathVariable Long taskId,
            @RequestParam TaskPriority priority) {
        return ResponseEntity.ok(new Response<>(taskManagementService.updatePriority(taskId, priority)));

    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<Response<List<TaskManagementDto>>> getTasksByPriority(@PathVariable TaskPriority priority) {
        return ResponseEntity.ok(new Response<>(taskManagementService.getTasksByPriority(priority)));

    }
    @PostMapping("/{taskId}/comment")
    public Response<String> addComment(
            @PathVariable Long taskId,
            @RequestBody AddCommentRequest request
    ) {
        taskManagementService.addComment(taskId, request.getUserId(), request.getComment());
        return new Response<>("Comment added successfully");
    }


    @GetMapping("/with-history/{taskId}")
    public Response<TaskManagementDto> getTaskWithHistory(@PathVariable Long taskId) {
        return new Response<>(taskManagementService.getTaskWithHistory(taskId));
    }

}


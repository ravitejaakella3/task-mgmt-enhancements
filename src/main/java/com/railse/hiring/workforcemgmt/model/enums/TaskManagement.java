package com.railse.hiring.workforcemgmt.model;

import com.railse.hiring.workforcemgmt.common.model.enums.ReferenceType;
import com.railse.hiring.workforcemgmt.model.enums.TaskPriority;
import com.railse.hiring.workforcemgmt.model.enums.Task;
import com.railse.hiring.workforcemgmt.model.enums.TaskStatus;
import lombok.Data;
import jakarta.persistence.Column;

@Data
public class TaskManagement {
    private Long id;
    private Long referenceId;
    private ReferenceType referenceType;
    private Task task;
    private String description;
    private TaskStatus status;
    private Long assigneeId;
    private Long taskDeadlineTime;
    private TaskPriority priority;
    @Column(name = "created_at")
    private Long createdAt;
}

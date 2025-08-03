package com.railse.hiring.workforcemgmt.model;

import lombok.Data;

@Data
public class TaskActivity {
    private Long id;
    private Long taskId;
    private String message;
    private Long timestamp;
}

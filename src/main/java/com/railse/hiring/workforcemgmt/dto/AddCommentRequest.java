package com.railse.hiring.workforcemgmt.dto;
import lombok.Data;

@Data
public class AddCommentRequest {
    private Long userId;
    private String comment;
}

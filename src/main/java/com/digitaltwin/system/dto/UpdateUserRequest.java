package com.digitaltwin.system.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String email;
    private String fullName;
}
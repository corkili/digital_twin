package com.digitaltwin.device.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private boolean success;
    private String message;
    private Object data;
    
    public static ApiResponse success(String message, Object data) {
        return new ApiResponse(true, message, data);
    }
    
    public static ApiResponse success(Object data) {
        return new ApiResponse(true, "操作成功", data);
    }
    
    public static ApiResponse error(String message) {
        return new ApiResponse(false, message, null);
    }
}
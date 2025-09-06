package com.digitaltwin.simulation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "仿真模块API响应结构")
public class SimulationApiResponse<T> {
    @Schema(description = "请求是否成功", example = "true")
    private boolean success;
    
    @Schema(description = "响应状态码", example = "200")
    private int code;
    
    @Schema(description = "响应消息", example = "操作成功")
    private String message;
    
    @Schema(description = "响应数据")
    private T data;
    
    public static <T> SimulationApiResponse<T> success(String message, T data) {
        SimulationApiResponse<T> response = new SimulationApiResponse<>();
        response.setSuccess(true);
        response.setCode(200);
        response.setMessage(message);
        response.setData(data);
        return response;
    }
    
    public static <T> SimulationApiResponse<T> success(T data) {
        SimulationApiResponse<T> response = new SimulationApiResponse<>();
        response.setSuccess(true);
        response.setCode(200);
        response.setMessage("操作成功");
        response.setData(data);
        return response;
    }
    
    public static <T> SimulationApiResponse<T> error(String message) {
        SimulationApiResponse<T> response = new SimulationApiResponse<>();
        response.setSuccess(false);
        response.setCode(10001);
        response.setMessage(message);
        response.setData(null);
        return response;
    }
}
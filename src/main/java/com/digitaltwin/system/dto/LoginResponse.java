package com.digitaltwin.system.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private UserDto user;
}
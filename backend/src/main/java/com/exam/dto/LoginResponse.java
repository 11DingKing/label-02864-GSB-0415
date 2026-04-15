package com.exam.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private Long userId;
    private String username;
    private String realName;
    private Integer role;
    private String token;
}

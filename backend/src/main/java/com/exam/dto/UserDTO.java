package com.exam.dto;

import lombok.Data;

@Data
public class UserDTO {
    private String username;
    private String password;
    private String realName;
    private Integer role;
    private Integer status;
}

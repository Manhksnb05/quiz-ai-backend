package com.hutech.quizbackend.model.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String email;
    private String name;
    private String role;
    private String provider;
}

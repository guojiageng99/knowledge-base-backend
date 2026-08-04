package com.knowledge.base.userauth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class TeamUpdateDTO implements Serializable {
    @NotNull(message = "Team ID must not be null")
    private Long id;
    @Size(max = 100, message = "Team name must not exceed 100 characters")
    private String teamName;
    @Size(max = 50, message = "Team code must not exceed 50 characters")
    private String teamCode;
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    private Long leaderId;
    private Integer status;
}

package com.knowledge.base.userauth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class TeamCreateDTO implements Serializable {
    @NotBlank(message = "Team name must not be blank")
    @Size(max = 100, message = "Team name must not exceed 100 characters")
    private String teamName;
    @NotBlank(message = "Team code must not be blank")
    @Size(max = 50, message = "Team code must not exceed 50 characters")
    private String teamCode;
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    private Long parentId;
    @NotNull(message = "Team leader must not be null")
    private Long leaderId;
}

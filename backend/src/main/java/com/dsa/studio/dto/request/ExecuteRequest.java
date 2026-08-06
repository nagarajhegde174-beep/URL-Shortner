package com.dsa.studio.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ExecuteRequest {
    @NotBlank(message = "Source code cannot be empty")
    private String code;

    @NotBlank(message = "Main class name is required")
    private String className;

    private String input; // Custom user inputs for stdin
}

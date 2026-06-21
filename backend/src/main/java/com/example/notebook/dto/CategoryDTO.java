package com.example.notebook.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryDTO {
    private Long id;
    
    @NotBlank(message = "目录名称不能为空")
    private String name;
    
    private Long userId;
    
    private Long parentId;
}
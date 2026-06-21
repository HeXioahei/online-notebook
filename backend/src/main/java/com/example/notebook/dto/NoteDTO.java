package com.example.notebook.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NoteDTO {
    private Long id;
    
    @NotBlank(message = "标题不能为空")
    private String title;
    
    private String content;
    
    private Long userId;
    
    private Long categoryId;
}
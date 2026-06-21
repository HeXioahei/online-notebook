package com.example.notebook.controller;

import com.example.notebook.dto.ApiResponse;
import com.example.notebook.dto.CategoryDTO;
import com.example.notebook.entity.Category;
import com.example.notebook.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    @GetMapping("/user/{userId}")
    public ApiResponse<List<Category>> getCategoriesByUserId(@PathVariable Long userId) {
        try {
            List<Category> categories = categoryService.getCategoriesByUserId(userId);
            return ApiResponse.success(categories);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ApiResponse<Category> getCategoryById(@PathVariable Long id) {
        try {
            Category category = categoryService.getCategoryById(id);
            return ApiResponse.success(category);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping
    public ApiResponse<Category> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        try {
            Category category = categoryService.createCategory(categoryDTO);
            return ApiResponse.success("创建成功", category);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ApiResponse<Category> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDTO categoryDTO) {
        try {
            Category category = categoryService.updateCategory(id, categoryDTO);
            return ApiResponse.success("更新成功", category);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id, @RequestParam Long userId) {
        try {
            categoryService.deleteCategory(id, userId);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
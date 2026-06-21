package com.example.notebook.service;

import com.example.notebook.dto.CategoryDTO;
import com.example.notebook.entity.Category;
import com.example.notebook.repository.CategoryRepository;
import com.example.notebook.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private NoteRepository noteRepository;
    
    public List<Category> getCategoriesByUserId(Long userId) {
        return categoryRepository.findByUserIdOrderByCreatedAtAsc(userId);
    }
    
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("目录不存在"));
    }
    
    public Category createCategory(CategoryDTO categoryDTO) {
        if (categoryRepository.existsByNameAndUserIdAndParentId(
            categoryDTO.getName(), 
            categoryDTO.getUserId(),
            categoryDTO.getParentId()
        )) {
            throw new RuntimeException("同级目录下已存在相同名称的目录");
        }
        
        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setUserId(categoryDTO.getUserId());
        category.setParentId(categoryDTO.getParentId());
        return categoryRepository.save(category);
    }
    
    public Category updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = getCategoryById(id);
        
        if (!category.getUserId().equals(categoryDTO.getUserId())) {
            throw new RuntimeException("无权修改此目录");
        }
        
        if (!category.getName().equals(categoryDTO.getName()) &&
            categoryRepository.existsByNameAndUserIdAndParentId(
                categoryDTO.getName(), 
                categoryDTO.getUserId(),
                category.getParentId()
            )) {
            throw new RuntimeException("同级目录下已存在相同名称的目录");
        }
        
        category.setName(categoryDTO.getName());
        category.setParentId(categoryDTO.getParentId());
        return categoryRepository.save(category);
    }
    
    public void deleteCategory(Long id, Long userId) {
        Category category = getCategoryById(id);
        
        if (!category.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此目录");
        }
        
        recursiveDeleteCategory(id);
    }
    
    private void recursiveDeleteCategory(Long categoryId) {
        List<Category> childCategories = categoryRepository.findByParentId(categoryId);
        
        for (Category child : childCategories) {
            recursiveDeleteCategory(child.getId());
        }
        
        noteRepository.deleteByCategoryId(categoryId);
        categoryRepository.deleteById(categoryId);
    }
}
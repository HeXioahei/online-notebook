package com.example.notebook.repository;

import com.example.notebook.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUserIdOrderByCreatedAtAsc(Long userId);
    boolean existsByNameAndUserId(String name, Long userId);
    
    boolean existsByNameAndUserIdAndParentId(String name, Long userId, Long parentId);
    
    List<Category> findByParentId(Long parentId);
}
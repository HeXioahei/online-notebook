package com.example.notebook.repository;

import com.example.notebook.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUserIdOrderByUpdatedAtDesc(Long userId);
    List<Note> findByUserIdAndCategoryIdOrderByUpdatedAtDesc(Long userId, Long categoryId);
    
    void deleteByCategoryId(Long categoryId);
}
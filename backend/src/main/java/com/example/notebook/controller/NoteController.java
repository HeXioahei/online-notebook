package com.example.notebook.controller;

import com.example.notebook.dto.ApiResponse;
import com.example.notebook.dto.NoteDTO;
import com.example.notebook.entity.Note;
import com.example.notebook.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {
    
    @Autowired
    private NoteService noteService;
    
    @GetMapping("/user/{userId}")
    public ApiResponse<List<Note>> getNotesByUserId(@PathVariable Long userId) {
        try {
            List<Note> notes = noteService.getNotesByUserId(userId);
            return ApiResponse.success(notes);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/user/{userId}/category/{categoryId}")
    public ApiResponse<List<Note>> getNotesByUserIdAndCategoryId(
            @PathVariable Long userId, 
            @PathVariable Long categoryId) {
        try {
            List<Note> notes = noteService.getNotesByUserIdAndCategoryId(userId, categoryId);
            return ApiResponse.success(notes);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public ApiResponse<Note> getNoteById(@PathVariable Long id) {
        try {
            Note note = noteService.getNoteById(id);
            return ApiResponse.success(note);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PostMapping
    public ApiResponse<Note> createNote(@Valid @RequestBody NoteDTO noteDTO) {
        try {
            Note note = noteService.createNote(noteDTO);
            return ApiResponse.success("创建成功", note);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ApiResponse<Note> updateNote(@PathVariable Long id, @Valid @RequestBody NoteDTO noteDTO) {
        try {
            Note note = noteService.updateNote(id, noteDTO);
            return ApiResponse.success("更新成功", note);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteNote(@PathVariable Long id, @RequestParam Long userId) {
        try {
            noteService.deleteNote(id, userId);
            return ApiResponse.success("删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
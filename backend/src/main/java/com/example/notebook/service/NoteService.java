package com.example.notebook.service;

import com.example.notebook.dto.NoteDTO;
import com.example.notebook.entity.Note;
import com.example.notebook.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class NoteService {
    
    @Autowired
    private NoteRepository noteRepository;
    
    public List<Note> getNotesByUserId(Long userId) {
        return noteRepository.findByUserIdOrderByUpdatedAtDesc(userId);
    }
    
    public List<Note> getNotesByUserIdAndCategoryId(Long userId, Long categoryId) {
        return noteRepository.findByUserIdAndCategoryIdOrderByUpdatedAtDesc(userId, categoryId);
    }
    
    public Note getNoteById(Long id) {
        return noteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("笔记不存在"));
    }
    
    public Note createNote(NoteDTO noteDTO) {
        Note note = new Note();
        note.setTitle(noteDTO.getTitle());
        note.setContent(noteDTO.getContent());
        note.setUserId(noteDTO.getUserId());
        note.setCategoryId(noteDTO.getCategoryId());
        return noteRepository.save(note);
    }
    
    public Note updateNote(Long id, NoteDTO noteDTO) {
        Note note = getNoteById(id);
        
        if (!note.getUserId().equals(noteDTO.getUserId())) {
            throw new RuntimeException("无权修改此笔记");
        }
        
        note.setTitle(noteDTO.getTitle());
        note.setContent(noteDTO.getContent());
        note.setCategoryId(noteDTO.getCategoryId());
        return noteRepository.save(note);
    }
    
    public void deleteNote(Long id, Long userId) {
        Note note = getNoteById(id);
        
        if (!note.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此笔记");
        }
        
        noteRepository.delete(note);
    }
}
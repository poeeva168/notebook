package com.notebook.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.notebook.dto.*;
import com.notebook.entity.Note;

public interface NoteService extends IService<Note> {

    NoteVO createNote(Long userId, NoteCreateRequest request);

    NoteVO updateNote(Long userId, Long noteId, NoteUpdateRequest request);

    NoteVO getNoteById(Long userId, Long noteId);

    PageResponse<NoteVO> getNoteList(Long userId, NoteQueryRequest request);

    void deleteNote(Long userId, Long noteId);

    void archiveNote(Long userId, Long noteId, Boolean isArchived);

    void restoreNote(Long userId, Long noteId);

    void permanentDeleteNote(Long userId, Long noteId);

    PageResponse<NoteVO> getTrashList(Long userId, Integer page, Integer size);
}

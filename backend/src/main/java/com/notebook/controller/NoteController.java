package com.notebook.controller;

import com.notebook.common.Result;
import com.notebook.dto.*;
import com.notebook.entity.User;
import com.notebook.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    public Result<NoteVO> createNote(@AuthenticationPrincipal User user,
                                     @Validated @RequestBody NoteCreateRequest request) {
        NoteVO note = noteService.createNote(user.getId(), request);
        return Result.success("笔记创建成功", note);
    }

    @PutMapping("/{id}")
    public Result<NoteVO> updateNote(@AuthenticationPrincipal User user,
                                    @PathVariable Long id,
                                    @Validated @RequestBody NoteUpdateRequest request) {
        NoteVO note = noteService.updateNote(user.getId(), id, request);
        return Result.success("笔记更新成功", note);
    }

    @GetMapping("/{id}")
    public Result<NoteVO> getNote(@AuthenticationPrincipal User user,
                                 @PathVariable Long id) {
        NoteVO note = noteService.getNoteById(user.getId(), id);
        return Result.success(note);
    }

    @GetMapping
    public Result<PageResponse<NoteVO>> getNoteList(@AuthenticationPrincipal User user,
                                                    NoteQueryRequest request) {
        PageResponse<NoteVO> response = noteService.getNoteList(user.getId(), request);
        return Result.success(response);
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteNote(@AuthenticationPrincipal User user,
                                     @PathVariable Long id) {
        noteService.deleteNote(user.getId(), id);
        return Result.success("笔记删除成功");
    }

    @PutMapping("/{id}/archive")
    public Result<String> archiveNote(@AuthenticationPrincipal User user,
                                      @PathVariable Long id,
                                      @RequestBody ArchiveRequest request) {
        noteService.archiveNote(user.getId(), id, request.getIsArchived());
        return Result.success(request.getIsArchived() ? "笔记归档成功" : "笔记取消归档成功");
    }

    @GetMapping("/trash")
    public Result<PageResponse<NoteVO>> getTrashList(@AuthenticationPrincipal User user,
                                                      @RequestParam(defaultValue = "1") Integer page,
                                                      @RequestParam(defaultValue = "20") Integer size) {
        PageResponse<NoteVO> response = noteService.getTrashList(user.getId(), page, size);
        return Result.success(response);
    }

    @PutMapping("/{id}/restore")
    public Result<String> restoreNote(@AuthenticationPrincipal User user,
                                     @PathVariable Long id) {
        noteService.restoreNote(user.getId(), id);
        return Result.success("笔记恢复成功");
    }

    @DeleteMapping("/{id}/permanent")
    public Result<String> permanentDeleteNote(@AuthenticationPrincipal User user,
                                              @PathVariable Long id) {
        noteService.permanentDeleteNote(user.getId(), id);
        return Result.success("笔记永久删除成功");
    }
}

package com.notebook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.notebook.common.BusinessException;
import com.notebook.dto.*;
import com.notebook.entity.*;
import com.notebook.mapper.*;
import com.notebook.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {

    private final NoteMapper noteMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final NoteTagMapper noteTagMapper;

    @Override
    @Transactional
    public NoteVO createNote(Long userId, NoteCreateRequest request) {
        Note note = new Note();
        note.setUserId(userId);
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setSummary(request.getSummary());
        note.setCategoryId(request.getCategoryId());
        note.setIsArchived(false);
        note.setViewCount(0);

        if (!StringUtils.hasText(note.getSummary()) && StringUtils.hasText(note.getContent())) {
            note.setSummary(note.getContent().length() > 200
                    ? note.getContent().substring(0, 200) : note.getContent());
        }

        noteMapper.insert(note);

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            saveNoteTags(note.getId(), request.getTagIds());
        }

        return getNoteVO(note.getId(), userId);
    }

    @Override
    @Transactional
    public NoteVO updateNote(Long userId, Long noteId, NoteUpdateRequest request) {
        Note note = getNoteByUserId(userId, noteId);

        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setSummary(request.getSummary());
        note.setCategoryId(request.getCategoryId());

        if (!StringUtils.hasText(note.getSummary()) && StringUtils.hasText(note.getContent())) {
            note.setSummary(note.getContent().length() > 200
                    ? note.getContent().substring(0, 200) : note.getContent());
        }

        noteMapper.updateById(note);

        LambdaQueryWrapper<NoteTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteTag::getNoteId, noteId);
        noteTagMapper.delete(wrapper);

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            saveNoteTags(noteId, request.getTagIds());
        }

        return getNoteVO(noteId, userId);
    }

    @Override
    public NoteVO getNoteById(Long userId, Long noteId) {
        Note note = getNoteByUserId(userId, noteId);
        return getNoteVO(noteId, userId);
    }

    @Override
    public PageResponse<NoteVO> getNoteList(Long userId, NoteQueryRequest request) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId)
               .eq(Note::getIsArchived, request.getIsArchived() != null ? request.getIsArchived() : false)
               .orderByDesc(Note::getUpdatedAt);

        if (request.getCategoryId() != null) {
            wrapper.eq(Note::getCategoryId, request.getCategoryId());
        }

        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w.like(Note::getTitle, request.getKeyword())
                             .or()
                             .like(Note::getContent, request.getKeyword()));
        }

        Page<Note> page = new Page<>(request.getPage(), request.getSize());
        Page<Note> result = noteMapper.selectPage(page, wrapper);

        List<NoteVO> voList = result.getRecords().stream()
                .map(note -> getNoteVO(note.getId(), userId))
                .collect(Collectors.toList());

        return new PageResponse<>(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public void deleteNote(Long userId, Long noteId) {
        Note note = getNoteByUserId(userId, noteId);
        note.setIsDeleted(true);
        noteMapper.updateById(note);
    }

    @Override
    public void archiveNote(Long userId, Long noteId, Boolean isArchived) {
        Note note = getNoteByUserId(userId, noteId);
        note.setIsArchived(isArchived);
        noteMapper.updateById(note);
    }

    @Override
    public void restoreNote(Long userId, Long noteId) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId)
               .eq(Note::getId, noteId)
               .eq(Note::getIsDeleted, true);

        Note note = noteMapper.selectOne(wrapper);
        if (note == null) {
            throw new BusinessException("笔记不存在或已被永久删除");
        }

        note.setIsDeleted(false);
        noteMapper.updateById(note);
    }

    @Override
    @Transactional
    public void permanentDeleteNote(Long userId, Long noteId) {
        Note note = getNoteByUserId(userId, noteId);

        LambdaQueryWrapper<NoteTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoteTag::getNoteId, noteId);
        noteTagMapper.delete(wrapper);

        noteMapper.deleteById(noteId);
    }

    @Override
    public PageResponse<NoteVO> getTrashList(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId)
               .eq(Note::getIsDeleted, true)
               .orderByDesc(Note::getUpdatedAt);

        Page<Note> result = noteMapper.selectPage(new Page<>(page, size), wrapper);

        List<NoteVO> voList = result.getRecords().stream()
                .map(note -> {
                    NoteVO vo = new NoteVO();
                    BeanUtils.copyProperties(note, vo);
                    return vo;
                })
                .collect(Collectors.toList());

        return new PageResponse<>(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    private Note getNoteByUserId(Long userId, Long noteId) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getId, noteId)
               .eq(Note::getUserId, userId)
               .eq(Note::getIsDeleted, false);

        Note note = noteMapper.selectOne(wrapper);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }

        return note;
    }

    private NoteVO getNoteVO(Long noteId, Long userId) {
        Note note = noteMapper.selectById(noteId);
        NoteVO vo = new NoteVO();
        BeanUtils.copyProperties(note, vo);

        if (note.getCategoryId() != null) {
            Category category = categoryMapper.selectById(note.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        LambdaQueryWrapper<NoteTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(NoteTag::getNoteId, noteId);
        List<NoteTag> noteTags = noteTagMapper.selectList(tagWrapper);

        if (!noteTags.isEmpty()) {
            List<Long> tagIds = noteTags.stream()
                    .map(NoteTag::getTagId)
                    .collect(Collectors.toList());

            List<Tag> tags = tagMapper.selectBatchIds(tagIds);
            List<TagVO> tagVOList = tags.stream()
                    .map(tag -> {
                        TagVO tagVO = new TagVO();
                        tagVO.setId(tag.getId());
                        tagVO.setName(tag.getName());
                        tagVO.setColor(tag.getColor());
                        return tagVO;
                    })
                    .collect(Collectors.toList());

            vo.setTags(tagVOList);
        } else {
            vo.setTags(new ArrayList<>());
        }

        return vo;
    }

    private void saveNoteTags(Long noteId, List<Long> tagIds) {
        for (Long tagId : tagIds) {
            NoteTag noteTag = new NoteTag();
            noteTag.setNoteId(noteId);
            noteTag.setTagId(tagId);
            noteTagMapper.insert(noteTag);
        }
    }
}

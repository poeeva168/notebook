package com.notebook.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.notebook.dto.TagRequest;
import com.notebook.dto.TagVO;
import com.notebook.entity.Tag;

import java.util.List;

public interface TagService extends IService<Tag> {

    TagVO createTag(Long userId, TagRequest request);

    TagVO updateTag(Long userId, Long tagId, TagRequest request);

    void deleteTag(Long userId, Long tagId);

    List<TagVO> getAllTags(Long userId);
}

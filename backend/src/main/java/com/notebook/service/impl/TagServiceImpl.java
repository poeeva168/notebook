package com.notebook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.notebook.common.BusinessException;
import com.notebook.dto.TagRequest;
import com.notebook.dto.TagVO;
import com.notebook.entity.Tag;
import com.notebook.mapper.TagMapper;
import com.notebook.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    private final TagMapper tagMapper;

    @Override
    public TagVO createTag(Long userId, TagRequest request) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getUserId, userId)
               .eq(Tag::getName, request.getName());

        if (tagMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("标签名称已存在");
        }

        Tag tag = new Tag();
        tag.setUserId(userId);
        tag.setName(request.getName());
        tag.setColor(request.getColor() != null ? request.getColor() : "#1890ff");

        tagMapper.insert(tag);

        TagVO vo = new TagVO();
        BeanUtils.copyProperties(tag, vo);
        return vo;
    }

    @Override
    public TagVO updateTag(Long userId, Long tagId, TagRequest request) {
        Tag tag = getTagByUserId(userId, tagId);

        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getUserId, userId)
               .eq(Tag::getName, request.getName())
               .ne(Tag::getId, tagId);

        if (tagMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("标签名称已存在");
        }

        tag.setName(request.getName());
        tag.setColor(request.getColor() != null ? request.getColor() : "#1890ff");

        tagMapper.updateById(tag);

        TagVO vo = new TagVO();
        BeanUtils.copyProperties(tag, vo);
        return vo;
    }

    @Override
    public void deleteTag(Long userId, Long tagId) {
        Tag tag = getTagByUserId(userId, tagId);
        tagMapper.deleteById(tagId);
    }

    @Override
    public List<TagVO> getAllTags(Long userId) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getUserId, userId)
               .orderByDesc(Tag::getId);

        List<Tag> tags = tagMapper.selectList(wrapper);

        return tags.stream()
                .map(tag -> {
                    TagVO vo = new TagVO();
                    BeanUtils.copyProperties(tag, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private Tag getTagByUserId(Long userId, Long tagId) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getId, tagId)
               .eq(Tag::getUserId, userId);

        Tag tag = tagMapper.selectOne(wrapper);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }

        return tag;
    }
}

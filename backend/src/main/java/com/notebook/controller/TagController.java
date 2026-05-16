package com.notebook.controller;

import com.notebook.common.Result;
import com.notebook.dto.TagRequest;
import com.notebook.dto.TagVO;
import com.notebook.entity.User;
import com.notebook.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping
    public Result<TagVO> createTag(@AuthenticationPrincipal User user,
                                   @Validated @RequestBody TagRequest request) {
        TagVO tag = tagService.createTag(user.getId(), request);
        return Result.success("标签创建成功", tag);
    }

    @PutMapping("/{id}")
    public Result<TagVO> updateTag(@AuthenticationPrincipal User user,
                                   @PathVariable Long id,
                                   @Validated @RequestBody TagRequest request) {
        TagVO tag = tagService.updateTag(user.getId(), id, request);
        return Result.success("标签更新成功", tag);
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteTag(@AuthenticationPrincipal User user,
                                   @PathVariable Long id) {
        tagService.deleteTag(user.getId(), id);
        return Result.success("标签删除成功");
    }

    @GetMapping
    public Result<List<TagVO>> getAllTags(@AuthenticationPrincipal User user) {
        List<TagVO> tags = tagService.getAllTags(user.getId());
        return Result.success(tags);
    }
}

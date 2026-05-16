package com.notebook.controller;

import com.notebook.common.Result;
import com.notebook.dto.CategoryRequest;
import com.notebook.dto.CategoryVO;
import com.notebook.entity.User;
import com.notebook.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public Result<CategoryVO> createCategory(@AuthenticationPrincipal User user,
                                             @Validated @RequestBody CategoryRequest request) {
        CategoryVO category = categoryService.createCategory(user.getId(), request);
        return Result.success("分类创建成功", category);
    }

    @PutMapping("/{id}")
    public Result<CategoryVO> updateCategory(@AuthenticationPrincipal User user,
                                            @PathVariable Long id,
                                            @Validated @RequestBody CategoryRequest request) {
        CategoryVO category = categoryService.updateCategory(user.getId(), id, request);
        return Result.success("分类更新成功", category);
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteCategory(@AuthenticationPrincipal User user,
                                       @PathVariable Long id) {
        categoryService.deleteCategory(user.getId(), id);
        return Result.success("分类删除成功");
    }

    @GetMapping
    public Result<List<CategoryVO>> getCategoryTree(@AuthenticationPrincipal User user) {
        List<CategoryVO> categories = categoryService.getCategoryTree(user.getId());
        return Result.success(categories);
    }
}

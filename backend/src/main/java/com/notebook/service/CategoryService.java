package com.notebook.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.notebook.dto.CategoryRequest;
import com.notebook.dto.CategoryVO;
import com.notebook.entity.Category;

import java.util.List;

public interface CategoryService extends IService<Category> {

    CategoryVO createCategory(Long userId, CategoryRequest request);

    CategoryVO updateCategory(Long userId, Long categoryId, CategoryRequest request);

    void deleteCategory(Long userId, Long categoryId);

    List<CategoryVO> getCategoryTree(Long userId);
}

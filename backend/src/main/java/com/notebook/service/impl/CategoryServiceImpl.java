package com.notebook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.notebook.common.BusinessException;
import com.notebook.dto.CategoryRequest;
import com.notebook.dto.CategoryVO;
import com.notebook.entity.Category;
import com.notebook.mapper.CategoryMapper;
import com.notebook.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    public CategoryVO createCategory(Long userId, CategoryRequest request) {
        Category category = new Category();
        category.setUserId(userId);
        category.setName(request.getName());
        category.setParentId(request.getParentId());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);

        categoryMapper.insert(category);

        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }

    @Override
    public CategoryVO updateCategory(Long userId, Long categoryId, CategoryRequest request) {
        Category category = getCategoryByUserId(userId, categoryId);

        category.setName(request.getName());
        category.setParentId(request.getParentId());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);

        categoryMapper.updateById(category);

        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }

    @Override
    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {
        Category category = getCategoryByUserId(userId, categoryId);

        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getParentId, categoryId);
        List<Category> children = categoryMapper.selectList(wrapper);

        if (!children.isEmpty()) {
            throw new BusinessException("该分类下有子分类，无法删除");
        }

        categoryMapper.deleteById(categoryId);
    }

    @Override
    public List<CategoryVO> getCategoryTree(Long userId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getUserId, userId)
               .orderByAsc(Category::getSortOrder)
               .orderByDesc(Category::getCreatedAt);

        List<Category> categories = categoryMapper.selectList(wrapper);

        List<CategoryVO> tree = buildTree(categories);

        return tree;
    }

    private Category getCategoryByUserId(Long userId, Long categoryId) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getId, categoryId)
               .eq(Category::getUserId, userId);

        Category category = categoryMapper.selectOne(wrapper);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        return category;
    }

    private List<CategoryVO> buildTree(List<Category> categories) {
        return categories.stream()
                .filter(c -> c.getParentId() == null)
                .map(c -> {
                    CategoryVO vo = convertToVO(c);
                    vo.setChildren(getChildren(c.getId(), categories));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private List<CategoryVO> getChildren(Long parentId, List<Category> allCategories) {
        return allCategories.stream()
                .filter(c -> parentId.equals(c.getParentId()))
                .map(c -> {
                    CategoryVO vo = convertToVO(c);
                    vo.setChildren(getChildren(c.getId(), allCategories));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private CategoryVO convertToVO(Category category) {
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        vo.setChildren(new ArrayList<>());
        return vo;
    }
}

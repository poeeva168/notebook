package com.notebook.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CategoryVO {

    private Long id;

    private String name;

    private Long parentId;

    private Integer sortOrder;

    private LocalDateTime createdAt;

    private List<CategoryVO> children;
}

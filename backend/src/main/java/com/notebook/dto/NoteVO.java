package com.notebook.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class NoteVO {

    private Long id;

    private String title;

    private String content;

    private String summary;

    private Long categoryId;

    private String categoryName;

    private Boolean isArchived;

    private Integer viewCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private List<TagVO> tags;
}

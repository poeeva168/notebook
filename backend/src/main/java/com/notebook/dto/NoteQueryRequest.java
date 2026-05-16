package com.notebook.dto;

import lombok.Data;

@Data
public class NoteQueryRequest {

    private Integer page = 1;

    private Integer size = 20;

    private Long categoryId;

    private Long tagId;

    private String keyword;

    private Boolean isArchived = false;
}

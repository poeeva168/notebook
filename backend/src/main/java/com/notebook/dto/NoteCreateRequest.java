package com.notebook.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class NoteCreateRequest {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String content;

    private String summary;

    private Long categoryId;

    private List<Long> tagIds;
}

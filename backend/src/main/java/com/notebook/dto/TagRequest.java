package com.notebook.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class TagRequest {

    @NotBlank(message = "标签名称不能为空")
    private String name;

    private String color = "#1890ff";
}

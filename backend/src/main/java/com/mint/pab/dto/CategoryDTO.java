package com.mint.pab.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class CategoryDTO {

    private Long id;

    private Long parentId;

    @NotBlank(message = "分类名称不能为空")
    @Size(min = 2, max = 20, message = "分类名称长度在2到20个字符之间")
    private String name;

    @NotBlank(message = "分类类型不能为空")
    private String type;

    private Integer sortOrder;

    private String color;

    private String icon;

}

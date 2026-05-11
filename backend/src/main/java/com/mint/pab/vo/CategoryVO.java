package com.mint.pab.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryVO {

    private Long id;

    private Long parentId;

    private String parentName;

    private String name;

    private String type;

    private String typeName;

    private Boolean isSystem;

    private Integer sortOrder;

    private String color;

    private String icon;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}

package com.mint.pab.vo;

import lombok.Data;

import java.util.List;

@Data
public class CategoryTreeVO {

    private Long id;

    private String name;

    private String type;

    private String typeName;

    private Boolean isSystem;

    private Integer sortOrder;

    private String color;

    private String icon;

    private List<Child> children;

    @Data
    public static class Child {

        private Long id;

        private String name;

        private String type;

        private String typeName;

        private Boolean isSystem;

        private Integer sortOrder;

        private String color;

        private String icon;

    }

}

package com.allen.pab.entity;

import lombok.Data;

import java.util.Date;

/**
 * 标签实体类
 *
 * @author Allen.Yang
 * @date 2025/12/4
 */
@Data
public class Tag {
    /**
     * 主键
     */
    private Integer id;
    /**
     * 标签名称
     */
    private String name;
    /**
     * 标签颜色（十六进制）
     */
    private String color;
    /**
     * 创建时间
     */
    private Date createdAt;
}

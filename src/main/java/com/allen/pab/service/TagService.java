package com.allen.pab.service;

import com.allen.pab.entity.Tag;

import java.util.List;

/**
 * 标签Service接口
 */
public interface TagService {
    /**
     * 创建标签
     */
    int createTag(Tag tag);

    /**
     * 删除标签
     */
    int deleteTag(Integer id);

    /**
     * 更新标签
     */
    int updateTag(Tag tag);

    /**
     * 根据ID获取标签
     */
    Tag getTagById(Integer id);

    /**
     * 获取所有标签
     */
    List<Tag> getAllTags();
}

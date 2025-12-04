package com.allen.pab.service.impl;

import com.allen.common.BusinessException;
import com.allen.common.ErrorCode;
import com.allen.pab.entity.Tag;
import com.allen.pab.mapper.TagMapper;
import com.allen.pab.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 标签Service实现类
 */
@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagMapper tagMapper;

    @Override
    public int createTag(Tag tag) {
        // 检查标签名称是否已存在
        Tag existingTag = tagMapper.selectByName(tag.getName());
        if (existingTag != null) {
            throw new BusinessException("标签名称已存在", ErrorCode.TAG_NAME_EXIST);
        }

        // 设置创建时间
        tag.setCreatedAt(new Date());

        // 如果没有设置颜色，使用默认颜色
        if (tag.getColor() == null) {
            tag.setColor("#FFFFFF");
        }

        return tagMapper.insert(tag);
    }

    @Override
    public int deleteTag(Integer id) {
        // 检查标签是否存在
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在", ErrorCode.TAG_NOT_EXIST);
        }

        int result = tagMapper.deleteById(id);
        if (result == 0) {
            throw new BusinessException("标签删除失败", ErrorCode.TAG_DELETE_FAILED);
        }

        return result;
    }

    @Override
    public int updateTag(Tag tag) {
        // 检查标签是否存在
        Tag existingTag = tagMapper.selectById(tag.getId());
        if (existingTag == null) {
            throw new BusinessException("标签不存在", ErrorCode.TAG_NOT_EXIST);
        }

        // 检查新名称是否与其他标签重复
        if (!existingTag.getName().equals(tag.getName())) {
            Tag nameConflict = tagMapper.selectByName(tag.getName());
            if (nameConflict != null) {
                throw new BusinessException("标签名称已存在", ErrorCode.TAG_NAME_EXIST);
            }
        }

        int result = tagMapper.update(tag);
        if (result == 0) {
            throw new BusinessException("标签更新失败", ErrorCode.TAG_UPDATE_FAILED);
        }

        return result;
    }

    @Override
    public Tag getTagById(Integer id) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在", ErrorCode.TAG_NOT_EXIST);
        }
        return tag;
    }

    @Override
    public List<Tag> getAllTags() {
        return tagMapper.selectAll();
    }
}

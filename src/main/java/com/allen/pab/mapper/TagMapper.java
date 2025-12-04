package com.allen.pab.mapper;

import com.allen.pab.entity.Tag;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 标签DAO接口
 */
@Mapper
public interface TagMapper {
    /**
     * 新增标签
     */
    int insert(Tag tag);

    /**
     * 根据ID删除标签
     */
    int deleteById(Integer id);

    /**
     * 更新标签
     */
    int update(Tag tag);

    /**
     * 根据ID查询标签
     */
    Tag selectById(Integer id);

    /**
     * 查询所有标签
     */
    List<Tag> selectAll();

    /**
     * 根据名称查询标签
     */
    Tag selectByName(String name);
}

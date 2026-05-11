package com.mint.pab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mint.pab.dto.CategoryDTO;
import com.mint.pab.entity.Category;
import com.mint.pab.entity.Transaction;
import com.mint.pab.enums.CategoryType;
import com.mint.pab.exception.BusinessException;
import com.mint.pab.exception.ErrorCode;
import com.mint.pab.repository.BudgetMapper;
import com.mint.pab.repository.CategoryMapper;
import com.mint.pab.repository.TransactionMapper;
import com.mint.pab.entity.Budget;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mint.pab.vo.CategoryTreeVO;
import com.mint.pab.vo.CategoryVO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private BudgetMapper budgetMapper;

    public List<CategoryVO> getCategories(Long userId, String type) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getIsDeleted, 0)
                .and(w -> w.eq(Category::getUserId, 0).or().eq(Category::getUserId, userId))
                .orderByAsc(Category::getSortOrder)
                .orderByAsc(Category::getCreateTime);
        if (type != null) {
            CategoryType typeEnum = CategoryType.getByName(type);
            if (typeEnum != null) {
                wrapper.eq(Category::getType, typeEnum.getCode());
            }
        }
        List<Category> categories = categoryMapper.selectList(wrapper);
        return categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    public List<CategoryTreeVO> getCategoryTree(Long userId, String type) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getIsDeleted, 0)
                .and(w -> w.eq(Category::getUserId, 0).or().eq(Category::getUserId, userId))
                .orderByAsc(Category::getSortOrder)
                .orderByAsc(Category::getCreateTime);
        if (type != null) {
            CategoryType typeEnum = CategoryType.getByName(type);
            if (typeEnum != null) {
                wrapper.eq(Category::getType, typeEnum.getCode());
            }
        }
        List<Category> categories = categoryMapper.selectList(wrapper);

        // 分离一级分类和二级分类
        List<Category> parents = categories.stream()
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.toList());
        Map<Long, List<Category>> childrenMap = categories.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(Category::getParentId, LinkedHashMap::new, Collectors.toList()));

        // 构建树
        List<CategoryTreeVO> tree = new ArrayList<>();
        for (Category parent : parents) {
            CategoryTreeVO treeVO = new CategoryTreeVO();
            treeVO.setId(parent.getId());
            treeVO.setName(parent.getName());
            CategoryType typeEnum = CategoryType.getByCode(parent.getType());
            treeVO.setType(typeEnum != null ? typeEnum.name() : null);
            treeVO.setTypeName(typeEnum != null ? typeEnum.getDesc() : "未知");
            treeVO.setIsSystem(Integer.valueOf(1).equals(parent.getIsSystem()));
            treeVO.setSortOrder(parent.getSortOrder());
            treeVO.setColor(parent.getColor());
            treeVO.setIcon(parent.getIcon());

            List<CategoryTreeVO.Child> children = new ArrayList<>();
            List<Category> childCategories = childrenMap.getOrDefault(parent.getId(), new ArrayList<>());
            for (Category child : childCategories) {
                CategoryTreeVO.Child childVO = new CategoryTreeVO.Child();
                childVO.setId(child.getId());
                childVO.setName(child.getName());
                CategoryType childTypeEnum = CategoryType.getByCode(child.getType());
                childVO.setType(childTypeEnum != null ? childTypeEnum.name() : null);
                childVO.setTypeName(childTypeEnum != null ? childTypeEnum.getDesc() : "未知");
                childVO.setIsSystem(Integer.valueOf(1).equals(child.getIsSystem()));
                childVO.setSortOrder(child.getSortOrder());
                childVO.setColor(child.getColor());
                childVO.setIcon(child.getIcon());
                children.add(childVO);
            }
            treeVO.setChildren(children);
            tree.add(treeVO);
        }

        return tree;
    }

    @Transactional(rollbackFor = Exception.class)
    public CategoryVO createCategory(Long userId, CategoryDTO dto) {
        // 1. 校验分类类型枚举值合法性
        CategoryType categoryType = CategoryType.getByName(dto.getType());
        if (categoryType == null) {
            throw new BusinessException(ErrorCode.CATEGORY_TYPE_INVALID);
        }

        if (dto.getParentId() != null) {
            // 创建二级分类
            // 2a. 校验父分类存在且为一级分类
            Category parent = categoryMapper.selectById(dto.getParentId());
            if (parent == null || parent.getIsDeleted() == 1) {
                throw new BusinessException(ErrorCode.PARENT_CATEGORY_NOT_FOUND);
            }
            if (parent.getParentId() != null) {
                throw new BusinessException(ErrorCode.PARENT_CATEGORY_MUST_BE_LEVEL1);
            }

            // 3a. 校验同名唯一：同 parent_id + name + type 不可重复
            LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Category::getParentId, dto.getParentId())
                    .eq(Category::getName, dto.getName())
                    .eq(Category::getType, categoryType.getCode())
                    .eq(Category::getIsDeleted, 0);
            Long count = categoryMapper.selectCount(wrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "该分类已存在");
            }
        } else {
            // 创建一级分类
            // 2b. 校验同 type 下同名一级分类不存在
            LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
            wrapper.isNull(Category::getParentId)
                    .eq(Category::getName, dto.getName())
                    .eq(Category::getType, categoryType.getCode())
                    .eq(Category::getIsDeleted, 0);
            Long count = categoryMapper.selectCount(wrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "该分类已存在");
            }
        }

        // 4. 设置 user_id = userId, is_system = 0
        Category category = new Category();
        category.setUserId(userId);
        category.setParentId(dto.getParentId());
        category.setName(dto.getName());
        category.setType(categoryType.getCode());
        category.setIsSystem(0);
        category.setSortOrder(dto.getSortOrder());
        category.setColor(dto.getColor());
        category.setIcon(dto.getIcon());
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());

        // 5. 插入数据库
        categoryMapper.insert(category);
        return convertToVO(category);
    }

    @Transactional(rollbackFor = Exception.class)
    public CategoryVO updateCategory(Long userId, Long id, CategoryDTO dto) {
        // 1. 查询分类，校验归属权
        Category category = categoryMapper.selectById(id);
        if (category == null || category.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "分类不存在");
        }
        if (!userId.equals(category.getUserId())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无权操作该分类");
        }

        // 2. 如果 is_system = 1，抛出 SYSTEM_CATEGORY_READONLY
        if (Integer.valueOf(1).equals(category.getIsSystem())) {
            throw new BusinessException(ErrorCode.SYSTEM_CATEGORY_READONLY);
        }

        // 3. 校验分类类型
        CategoryType categoryType = CategoryType.getByName(dto.getType());
        if (categoryType == null) {
            throw new BusinessException(ErrorCode.CATEGORY_TYPE_INVALID);
        }

        // 4. 校验新名称唯一性（排除自身）
        if (dto.getParentId() != null) {
            // 二级分类唯一性
            LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Category::getParentId, dto.getParentId())
                    .eq(Category::getName, dto.getName())
                    .eq(Category::getType, categoryType.getCode())
                    .eq(Category::getIsDeleted, 0)
                    .ne(Category::getId, id);
            Long count = categoryMapper.selectCount(wrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "该分类已存在");
            }
        } else {
            // 一级分类唯一性
            LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
            wrapper.isNull(Category::getParentId)
                    .eq(Category::getName, dto.getName())
                    .eq(Category::getType, categoryType.getCode())
                    .eq(Category::getIsDeleted, 0)
                    .ne(Category::getId, id);
            Long count = categoryMapper.selectCount(wrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "该分类已存在");
            }
        }

        // 5. 如果修改 parentId，需要校验
        if (dto.getParentId() != null && !dto.getParentId().equals(category.getParentId())) {
            // 从一级分类移动到二级分类
            Category parent = categoryMapper.selectById(dto.getParentId());
            if (parent == null || parent.getIsDeleted() == 1) {
                throw new BusinessException(ErrorCode.PARENT_CATEGORY_NOT_FOUND);
            }
            if (parent.getParentId() != null) {
                throw new BusinessException(ErrorCode.PARENT_CATEGORY_MUST_BE_LEVEL1);
            }
            // 如果原来是一级分类，移动到二级分类时，需检查其下是否有子分类
            if (category.getParentId() == null) {
                LambdaQueryWrapper<Category> childWrapper = new LambdaQueryWrapper<>();
                childWrapper.eq(Category::getParentId, id)
                        .eq(Category::getIsDeleted, 0);
                Long childCount = categoryMapper.selectCount(childWrapper);
                if (childCount > 0) {
                    throw new BusinessException(ErrorCode.CATEGORY_HAS_CHILDREN, "该分类下存在子分类，无法移动为二级分类");
                }
            }
        }

        // 6. 更新字段
        category.setParentId(dto.getParentId());
        category.setName(dto.getName());
        category.setType(categoryType.getCode());
        category.setSortOrder(dto.getSortOrder());
        category.setColor(dto.getColor());
        category.setIcon(dto.getIcon());
        category.setUpdateTime(LocalDateTime.now());
        categoryMapper.updateById(category);
        return convertToVO(category);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long userId, Long id) {
        // 1. 查询分类，校验归属权
        Category category = categoryMapper.selectById(id);
        if (category == null || category.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "分类不存在");
        }
        if (!userId.equals(category.getUserId())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无权操作该分类");
        }

        // 2. 如果 is_system = 1，抛出 SYSTEM_CATEGORY_READONLY
        if (Integer.valueOf(1).equals(category.getIsSystem())) {
            throw new BusinessException(ErrorCode.SYSTEM_CATEGORY_READONLY);
        }

        // 3. 如果是一级分类，检查是否有子分类
        if (category.getParentId() == null) {
            LambdaQueryWrapper<Category> childWrapper = new LambdaQueryWrapper<>();
            childWrapper.eq(Category::getParentId, id)
                    .eq(Category::getIsDeleted, 0);
            Long childCount = categoryMapper.selectCount(childWrapper);
            if (childCount > 0) {
                throw new BusinessException(ErrorCode.CATEGORY_HAS_CHILDREN);
            }
        }

        // 4. 检查是否有关联交易
        LambdaQueryWrapper<Transaction> txWrapper = new LambdaQueryWrapper<>();
        txWrapper.eq(Transaction::getCategoryId, id)
                .eq(Transaction::getIsDeleted, 0);
        Long txCount = transactionMapper.selectCount(txWrapper);
        if (txCount > 0) {
            throw new BusinessException(ErrorCode.CATEGORY_HAS_TRANSACTION);
        }

        // 5. 检查是否有关联预算（一级分类可能被预算引用）
        LambdaQueryWrapper<Budget> budgetWrapper = new LambdaQueryWrapper<>();
        budgetWrapper.eq(Budget::getCategoryId, id)
                .eq(Budget::getIsDeleted, 0);
        Long budgetCount = budgetMapper.selectCount(budgetWrapper);
        if (budgetCount > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该分类下存在关联预算，不允许删除");
        }

        // 6. 逻辑删除
        categoryMapper.deleteById(id);
    }

    /**
     * 将 Category 实体转换为 CategoryVO，将 Integer 类型的 type 转换为 String 枚举名
     */
    private CategoryVO convertToVO(Category category) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setParentId(category.getParentId());

        // 填充 parentName（如果是一级分类则为 null）
        if (category.getParentId() != null) {
            Category parent = categoryMapper.selectById(category.getParentId());
            vo.setParentName(parent != null ? parent.getName() : null);
        }

        vo.setName(category.getName());

        // 转换 type: Integer code -> String 枚举名
        CategoryType typeEnum = CategoryType.getByCode(category.getType());
        vo.setType(typeEnum != null ? typeEnum.name() : null);
        vo.setTypeName(typeEnum != null ? typeEnum.getDesc() : "未知");

        vo.setIsSystem(Integer.valueOf(1).equals(category.getIsSystem()));
        vo.setSortOrder(category.getSortOrder());
        vo.setColor(category.getColor());
        vo.setIcon(category.getIcon());
        vo.setCreateTime(category.getCreateTime());
        vo.setUpdateTime(category.getUpdateTime());
        return vo;
    }
}

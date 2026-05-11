package com.mint.pab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mint.pab.dto.BudgetDTO;
import com.mint.pab.entity.Budget;
import com.mint.pab.entity.Category;
import com.mint.pab.entity.Transaction;
import com.mint.pab.enums.BudgetType;
import com.mint.pab.enums.TransactionType;
import com.mint.pab.exception.BusinessException;
import com.mint.pab.exception.ErrorCode;
import com.mint.pab.repository.BudgetMapper;
import com.mint.pab.repository.CategoryMapper;
import com.mint.pab.repository.TransactionMapper;
import com.mint.pab.vo.BudgetExecutionVO;
import com.mint.pab.vo.BudgetVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BudgetService {

    @Autowired
    private BudgetMapper budgetMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    public List<BudgetVO> getBudgets(Long userId, String month) {
        LambdaQueryWrapper<Budget> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Budget::getUserId, userId)
                .eq(Budget::getMonth, month)
                .eq(Budget::getIsDeleted, 0)
                .orderByAsc(Budget::getType)
                .orderByAsc(Budget::getCategoryId);
        List<Budget> budgets = budgetMapper.selectList(wrapper);
        return budgets.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    public BudgetVO createBudget(Long userId, BudgetDTO dto) {
        // 1. 校验金额 > 0
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.BUDGET_AMOUNT_INVALID);
        }

        // 2. 校验预算类型枚举值合法性
        BudgetType budgetType = BudgetType.getByName(dto.getType());
        if (budgetType == null) {
            throw new BusinessException(ErrorCode.BUDGET_TYPE_INVALID);
        }

        // 3. 校验同月份同类型（同分类）不可重复
        LambdaQueryWrapper<Budget> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Budget::getUserId, userId)
                .eq(Budget::getMonth, dto.getMonth())
                .eq(Budget::getType, budgetType.getCode());
        if (BudgetType.CATEGORY.getCode().equals(budgetType.getCode())) {
            wrapper.eq(Budget::getCategoryId, dto.getCategoryId());
            // 校验分类预算的 categoryId 必须是一级分类
            if (dto.getCategoryId() != null) {
                Category category = categoryMapper.selectById(dto.getCategoryId());
                if (category == null || category.getIsDeleted() == 1) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "分类不存在");
                }
                if (category.getParentId() != null) {
                    throw new BusinessException(ErrorCode.PARENT_CATEGORY_MUST_BE_LEVEL1);
                }
            }
        }
        wrapper.eq(Budget::getIsDeleted, 0);
        Budget existing = budgetMapper.selectOne(wrapper);
        if (existing != null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该月份已存在相同类型的预算，请勿重复设置");
        }

        // 4. 插入数据库
        Budget budget = new Budget();
        budget.setUserId(userId);
        budget.setMonth(dto.getMonth());
        budget.setType(budgetType.getCode());
        budget.setCategoryId(dto.getCategoryId());
        budget.setAmount(dto.getAmount());
        budget.setCreateTime(LocalDateTime.now());
        budget.setUpdateTime(LocalDateTime.now());
        budgetMapper.insert(budget);
        return convertToVO(budget);
    }

    public BudgetVO updateBudget(Long userId, Long id, BudgetDTO dto) {
        // 1. 查询预算，校验归属权
        Budget budget = budgetMapper.selectById(id);
        if (budget == null || budget.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "预算记录不存在");
        }
        if (!userId.equals(budget.getUserId())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无权操作该预算记录");
        }

        // 2. 校验金额 > 0
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.BUDGET_AMOUNT_INVALID);
        }

        // 3. 更新金额
        budget.setAmount(dto.getAmount());
        budget.setUpdateTime(LocalDateTime.now());
        budgetMapper.updateById(budget);
        return convertToVO(budget);
    }

    public void deleteBudget(Long userId, Long id) {
        // 1. 查询预算，校验归属权
        Budget budget = budgetMapper.selectById(id);
        if (budget == null || budget.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "预算记录不存在");
        }
        if (!userId.equals(budget.getUserId())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无权操作该预算记录");
        }

        // 2. 逻辑删除
        budgetMapper.deleteById(id);
    }

    public BudgetExecutionVO getBudgetExecution(Long userId, String month) {
        BudgetExecutionVO vo = new BudgetExecutionVO();
        vo.setMonth(month);

        // 1. 查询该月总预算和分类预算（直接查询实体，不走 VO 转换）
        LambdaQueryWrapper<Budget> budgetWrapper = new LambdaQueryWrapper<>();
        budgetWrapper.eq(Budget::getUserId, userId)
                .eq(Budget::getMonth, month)
                .eq(Budget::getIsDeleted, 0);
        List<Budget> budgets = budgetMapper.selectList(budgetWrapper);
        Budget totalBudget = budgets.stream()
                .filter(b -> BudgetType.TOTAL.getCode().equals(b.getType()))
                .findFirst()
                .orElse(null);
        List<Budget> categoryBudgets = budgets.stream()
                .filter(b -> BudgetType.CATEGORY.getCode().equals(b.getType()))
                .collect(Collectors.toList());

        BigDecimal totalBudgetAmount = totalBudget != null ? totalBudget.getAmount() : BigDecimal.ZERO;
        vo.setTotalBudget(totalBudgetAmount);

        // 2. 计算总已用金额：查询该月所有支出交易的总金额
        YearMonth yearMonth = YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDateTime startTime = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endTime = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        LambdaQueryWrapper<Transaction> txWrapper = new LambdaQueryWrapper<>();
        txWrapper.eq(Transaction::getUserId, userId)
                .eq(Transaction::getType, TransactionType.EXPENSE.getCode())
                .ge(Transaction::getTransactionTime, startTime)
                .le(Transaction::getTransactionTime, endTime)
                .eq(Transaction::getIsDeleted, 0);
        List<Transaction> expenseTransactions = transactionMapper.selectList(txWrapper);

        BigDecimal totalUsed = expenseTransactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalUsed(totalUsed);

        BigDecimal totalRemaining = totalBudgetAmount.subtract(totalUsed);
        vo.setTotalRemaining(totalRemaining);

        BigDecimal totalUsageRate = BigDecimal.ZERO;
        if (totalBudgetAmount.compareTo(BigDecimal.ZERO) > 0) {
            totalUsageRate = totalUsed.multiply(new BigDecimal("100"))
                    .divide(totalBudgetAmount, 2, RoundingMode.HALF_UP);
        }
        vo.setTotalUsageRate(totalUsageRate);
        vo.setTotalStatus(calculateStatus(totalUsageRate));

        // 3. 计算各分类已用金额：预算关联一级分类，查找该一级分类下所有二级分类的交易汇总
        // 先构建 categoryId -> 二级分类ID列表 的映射
        Map<Long, List<Long>> parentToChildrenMap = categoryBudgets.stream()
                .collect(Collectors.toMap(
                        Budget::getCategoryId,
                        b -> {
                            LambdaQueryWrapper<Category> catWrapper = new LambdaQueryWrapper<>();
                            catWrapper.eq(Category::getParentId, b.getCategoryId())
                                    .eq(Category::getIsDeleted, 0);
                            return categoryMapper.selectList(catWrapper).stream()
                                    .map(Category::getId)
                                    .collect(Collectors.toList());
                        }
                ));

        // 按二级分类ID分组统计支出
        Map<Long, List<Transaction>> expenseByCategoryId = expenseTransactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCategoryId));

        List<BudgetExecutionVO.CategoryBudgetVO> categoryBudgetVOList = new ArrayList<>();
        for (Budget categoryBudget : categoryBudgets) {
            BudgetExecutionVO.CategoryBudgetVO cbvo = new BudgetExecutionVO.CategoryBudgetVO();
            cbvo.setCategoryId(categoryBudget.getCategoryId());

            Category category = categoryMapper.selectById(categoryBudget.getCategoryId());
            cbvo.setCategoryParentName(category != null ? category.getName() : "其他");
            cbvo.setBudgetAmount(categoryBudget.getAmount());

            // 汇总该一级分类下所有二级分类的交易金额
            List<Long> childIds = parentToChildrenMap.getOrDefault(categoryBudget.getCategoryId(), new ArrayList<>());
            BigDecimal used = BigDecimal.ZERO;
            for (Long childId : childIds) {
                List<Transaction> txs = expenseByCategoryId.getOrDefault(childId, new ArrayList<>());
                BigDecimal childUsed = txs.stream()
                        .map(Transaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                used = used.add(childUsed);
            }
            cbvo.setUsedAmount(used);
            cbvo.setRemainingAmount(categoryBudget.getAmount().subtract(used));

            BigDecimal rate = BigDecimal.ZERO;
            if (categoryBudget.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                rate = used.multiply(new BigDecimal("100"))
                        .divide(categoryBudget.getAmount(), 2, RoundingMode.HALF_UP);
            }
            cbvo.setUsageRate(rate);
            cbvo.setStatus(calculateStatus(rate));

            categoryBudgetVOList.add(cbvo);
        }
        vo.setCategoryBudgets(categoryBudgetVOList);

        return vo;
    }

    private String calculateStatus(BigDecimal usageRate) {
        if (usageRate.compareTo(new BigDecimal("80")) < 0) {
            return "NORMAL";
        } else if (usageRate.compareTo(new BigDecimal("100")) < 0) {
            return "WARNING";
        } else {
            return "OVER";
        }
    }

    /**
     * 将 Budget 实体转换为 BudgetVO，将 Integer 类型的 type 转换为 String 枚举名
     */
    private BudgetVO convertToVO(Budget budget) {
        BudgetVO vo = new BudgetVO();
        vo.setId(budget.getId());
        vo.setMonth(budget.getMonth());

        // 转换 type: Integer code -> String 枚举名
        BudgetType typeEnum = BudgetType.getByCode(budget.getType());
        vo.setType(typeEnum != null ? typeEnum.name() : null);
        vo.setTypeName(typeEnum != null ? typeEnum.getDesc() : "未知");

        vo.setCategoryId(budget.getCategoryId());

        // 填充分类名称（预算关联一级分类）
        if (budget.getCategoryId() != null) {
            Category category = categoryMapper.selectById(budget.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        vo.setAmount(budget.getAmount());
        vo.setCreateTime(budget.getCreateTime());
        vo.setUpdateTime(budget.getUpdateTime());
        return vo;
    }

}

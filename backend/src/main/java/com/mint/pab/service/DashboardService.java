package com.mint.pab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mint.pab.entity.Budget;
import com.mint.pab.entity.Transaction;
import com.mint.pab.enums.BudgetType;
import com.mint.pab.enums.TransactionType;
import com.mint.pab.repository.BudgetMapper;
import com.mint.pab.repository.TransactionMapper;
import com.mint.pab.vo.DashboardVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class DashboardService {

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private BudgetMapper budgetMapper;

    public DashboardVO getSummary(Long userId) {
        DashboardVO vo = new DashboardVO();
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(23, 59, 59);

        // 1. 今日统计
        List<Transaction> todayTransactions = queryTransactions(userId, todayStart, todayEnd);
        vo.setTodayIncome(sumByType(todayTransactions, TransactionType.INCOME.getCode()));
        vo.setTodayExpense(sumByType(todayTransactions, TransactionType.EXPENSE.getCode()));
        vo.setTodayCount(todayTransactions.size());

        // 2. 本周统计（周一到今天）
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDateTime weekStartTime = weekStart.atStartOfDay();
        List<Transaction> weekTransactions = queryTransactions(userId, weekStartTime, todayEnd);
        BigDecimal weekIncome = sumByType(weekTransactions, TransactionType.INCOME.getCode());
        BigDecimal weekExpense = sumByType(weekTransactions, TransactionType.EXPENSE.getCode());
        vo.setWeekIncome(weekIncome);
        vo.setWeekExpense(weekExpense);
        vo.setWeekCount(weekTransactions.size());

        int daysPassed = today.getDayOfWeek().getValue();
        if (daysPassed > 0 && weekExpense.compareTo(BigDecimal.ZERO) > 0) {
            vo.setWeekDailyAvgExpense(weekExpense.divide(new BigDecimal(daysPassed), 2, RoundingMode.HALF_UP));
        } else {
            vo.setWeekDailyAvgExpense(BigDecimal.ZERO);
        }

        // 3. 本月统计
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDateTime monthStartTime = monthStart.atStartOfDay();
        List<Transaction> monthTransactions = queryTransactions(userId, monthStartTime, todayEnd);
        BigDecimal monthIncome = sumByType(monthTransactions, TransactionType.INCOME.getCode());
        BigDecimal monthExpense = sumByType(monthTransactions, TransactionType.EXPENSE.getCode());
        vo.setMonthIncome(monthIncome);
        vo.setMonthExpense(monthExpense);
        vo.setMonthBalance(monthIncome.subtract(monthExpense));

        // 4. 预算执行率
        String monthStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        LambdaQueryWrapper<Budget> budgetWrapper = new LambdaQueryWrapper<>();
        budgetWrapper.eq(Budget::getUserId, userId)
                .eq(Budget::getMonth, monthStr)
                .eq(Budget::getType, BudgetType.TOTAL.getCode())
                .eq(Budget::getIsDeleted, 0);
        Budget totalBudget = budgetMapper.selectOne(budgetWrapper);

        if (totalBudget != null && totalBudget.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal budgetUsageRate = monthExpense.multiply(new BigDecimal("100"))
                    .divide(totalBudget.getAmount(), 2, RoundingMode.HALF_UP);
            vo.setBudgetUsageRate(budgetUsageRate);
            vo.setBudgetStatus(calculateBudgetStatus(budgetUsageRate));
        } else {
            vo.setBudgetUsageRate(null);
            vo.setBudgetStatus(null);
        }

        return vo;
    }

    private List<Transaction> queryTransactions(Long userId, LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getUserId, userId)
                .ge(Transaction::getTransactionTime, start)
                .le(Transaction::getTransactionTime, end)
                .eq(Transaction::getIsDeleted, 0);
        return transactionMapper.selectList(wrapper);
    }

    private BigDecimal sumByType(List<Transaction> list, Integer type) {
        return list.stream()
                .filter(t -> type.equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String calculateBudgetStatus(BigDecimal usageRate) {
        if (usageRate.compareTo(new BigDecimal("80")) < 0) {
            return "NORMAL";
        } else if (usageRate.compareTo(new BigDecimal("100")) < 0) {
            return "WARNING";
        } else {
            return "OVER";
        }
    }

}

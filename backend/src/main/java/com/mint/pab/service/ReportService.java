package com.mint.pab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mint.pab.entity.Account;
import com.mint.pab.entity.Category;
import com.mint.pab.entity.Transaction;
import com.mint.pab.enums.AccountStatus;
import com.mint.pab.enums.AccountType;
import com.mint.pab.enums.TransactionType;
import com.mint.pab.repository.AccountMapper;
import com.mint.pab.repository.CategoryMapper;
import com.mint.pab.repository.TransactionMapper;
import com.mint.pab.vo.BalanceSheetVO;
import com.mint.pab.vo.CashFlowVO;
import com.mint.pab.vo.IncomeExpenseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ReportService {

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    public BalanceSheetVO getBalanceSheet(Long userId, String date) {
        BalanceSheetVO vo = new BalanceSheetVO();

        // 解析查询日期（当天结束时间）
        LocalDateTime queryDate = LocalDate.parse(date).atTime(23, 59, 59);

        // 查询用户所有正常状态的账户
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Account::getUserId, userId)
                .eq(Account::getStatus, AccountStatus.ACTIVE.getCode())
                .eq(Account::getIsDeleted, 0);
        List<Account> accounts = accountMapper.selectList(wrapper);

        // 查询指定日期之后的所有交易，用于回退当前余额到历史余额
        LambdaQueryWrapper<Transaction> txWrapper = new LambdaQueryWrapper<>();
        txWrapper.eq(Transaction::getUserId, userId)
                .gt(Transaction::getTransactionTime, queryDate)
                .eq(Transaction::getIsDeleted, 0);
        List<Transaction> afterTransactions = transactionMapper.selectList(txWrapper);

        // 按账户汇总指定日期之后的交易影响
        Map<Long, BigDecimal> accountAdjustMap = new java.util.HashMap<>();
        for (Transaction tx : afterTransactions) {
            BigDecimal amount = tx.getAmount();
            // 作为转出方（支出/转账）：当时余额减少了amount，回退时需要加回来
            Long fromAccountId = tx.getFromAccountId();
            if (fromAccountId != null) {
                accountAdjustMap.merge(fromAccountId, amount, BigDecimal::add);
            }
            // 作为转入方（收入/转账）：当时余额增加了amount，回退时需要减回去
            Long toAccountId = tx.getToAccountId();
            if (toAccountId != null) {
                accountAdjustMap.merge(toAccountId, amount.negate(), BigDecimal::add);
            }
        }

        // 按账户类型分组
        Map<Integer, List<Account>> groupMap = accounts.stream()
                .collect(Collectors.groupingBy(Account::getType));

        List<BalanceSheetVO.AccountGroupVO> accountGroups = new ArrayList<>();
        BigDecimal totalAssets = BigDecimal.ZERO;

        for (Map.Entry<Integer, List<Account>> entry : groupMap.entrySet()) {
            BalanceSheetVO.AccountGroupVO groupVO = new BalanceSheetVO.AccountGroupVO();
            AccountType typeEnum = AccountType.getByCode(entry.getKey());
            groupVO.setType(typeEnum != null ? typeEnum.name() : String.valueOf(entry.getKey()));
            groupVO.setTypeName(typeEnum != null ? typeEnum.getDesc() : "未知");

            List<BalanceSheetVO.AccountItemVO> itemList = new ArrayList<>();
            BigDecimal groupBalance = BigDecimal.ZERO;

            for (Account account : entry.getValue()) {
                BalanceSheetVO.AccountItemVO item = new BalanceSheetVO.AccountItemVO();
                item.setId(account.getId());
                item.setName(account.getName());
                AccountType itemTypeEnum = AccountType.getByCode(account.getType());
                item.setType(itemTypeEnum != null ? itemTypeEnum.name() : null);

                // 计算指定日期的历史余额：当前余额 + 回退调整值
                BigDecimal adjust = accountAdjustMap.getOrDefault(account.getId(), BigDecimal.ZERO);
                BigDecimal historyBalance = account.getCurrentBalance().add(adjust);
                item.setCurrentBalance(historyBalance);

                itemList.add(item);
                groupBalance = groupBalance.add(historyBalance);
            }

            groupVO.setAccounts(itemList);
            groupVO.setTotalBalance(groupBalance);
            accountGroups.add(groupVO);
            totalAssets = totalAssets.add(groupBalance);
        }

        vo.setTotalAssets(totalAssets);
        vo.setAccountGroups(accountGroups);
        return vo;
    }

    public IncomeExpenseVO getIncomeExpense(Long userId, String month) {
        IncomeExpenseVO vo = new IncomeExpenseVO();
        vo.setMonth(month);

        YearMonth yearMonth = YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDateTime startTime = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endTime = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        // 查询指定月份的所有交易
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getUserId, userId)
                .in(Transaction::getType, TransactionType.INCOME.getCode(), TransactionType.EXPENSE.getCode())
                .ge(Transaction::getTransactionTime, startTime)
                .le(Transaction::getTransactionTime, endTime)
                .eq(Transaction::getIsDeleted, 0);
        List<Transaction> transactions = transactionMapper.selectList(wrapper);

        // 收入按分类汇总
        List<Transaction> incomeList = transactions.stream()
                .filter(t -> TransactionType.INCOME.getCode().equals(t.getType()))
                .collect(Collectors.toList());
        List<Transaction> expenseList = transactions.stream()
                .filter(t -> TransactionType.EXPENSE.getCode().equals(t.getType()))
                .collect(Collectors.toList());

        BigDecimal totalIncome = incomeList.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpense = expenseList.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        vo.setTotalIncome(totalIncome);
        vo.setTotalExpense(totalExpense);
        vo.setBalance(totalIncome.subtract(totalExpense));

        // 收入分类汇总
        List<IncomeExpenseVO.CategoryAmountVO> incomeItems = summarizeByCategory(incomeList, totalIncome);
        List<IncomeExpenseVO.CategoryAmountVO> expenseItems = summarizeByCategory(expenseList, totalExpense);

        vo.setIncomeItems(incomeItems);
        vo.setExpenseItems(expenseItems);
        return vo;
    }

    private List<IncomeExpenseVO.CategoryAmountVO> summarizeByCategory(List<Transaction> list, BigDecimal total) {
        Map<Long, List<Transaction>> groupByCategory = list.stream()
                .collect(Collectors.groupingBy(Transaction::getCategoryId));

        List<IncomeExpenseVO.CategoryAmountVO> result = new ArrayList<>();
        for (Map.Entry<Long, List<Transaction>> entry : groupByCategory.entrySet()) {
            IncomeExpenseVO.CategoryAmountVO item = new IncomeExpenseVO.CategoryAmountVO();
            BigDecimal amount = entry.getValue().stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Category category = categoryMapper.selectById(entry.getKey());
            if (category != null) {
                if (category.getParentId() != null) {
                    Category parent = categoryMapper.selectById(category.getParentId());
                    item.setCategoryParentName(parent != null ? parent.getName() : "其他");
                } else {
                    item.setCategoryParentName(null);
                }
                item.setCategoryName(category.getName());
            } else {
                item.setCategoryParentName("其他");
                item.setCategoryName("其他");
            }
            item.setAmount(amount);

            BigDecimal percentage = BigDecimal.ZERO;
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                percentage = amount.multiply(new BigDecimal("100"))
                        .divide(total, 2, RoundingMode.HALF_UP);
            }
            item.setPercentage(percentage);
            result.add(item);
        }

        // 按金额降序排列
        result.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));
        return result;
    }

    public CashFlowVO getCashFlow(Long userId, String month) {
        CashFlowVO vo = new CashFlowVO();
        vo.setMonth(month);

        YearMonth yearMonth = YearMonth.parse(month, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDateTime startTime = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endTime = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        // 查询用户所有正常账户
        LambdaQueryWrapper<Account> accountWrapper = new LambdaQueryWrapper<>();
        accountWrapper.eq(Account::getUserId, userId)
                .eq(Account::getStatus, AccountStatus.ACTIVE.getCode())
                .eq(Account::getIsDeleted, 0);
        List<Account> accounts = accountMapper.selectList(accountWrapper);

        // 查询该月所有交易
        LambdaQueryWrapper<Transaction> txWrapper = new LambdaQueryWrapper<>();
        txWrapper.eq(Transaction::getUserId, userId)
                .ge(Transaction::getTransactionTime, startTime)
                .le(Transaction::getTransactionTime, endTime)
                .eq(Transaction::getIsDeleted, 0);
        List<Transaction> transactions = transactionMapper.selectList(txWrapper);

        List<CashFlowVO.AccountFlowVO> accountFlows = new ArrayList<>();
        BigDecimal totalInflow = BigDecimal.ZERO;
        BigDecimal totalOutflow = BigDecimal.ZERO;

        for (Account account : accounts) {
            CashFlowVO.AccountFlowVO flow = new CashFlowVO.AccountFlowVO();
            flow.setAccountId(account.getId());
            flow.setAccountName(account.getName());
            AccountType flowTypeEnum = AccountType.getByCode(account.getType());
            flow.setAccountType(flowTypeEnum != null ? flowTypeEnum.name() : null);

            Long accountId = account.getId();

            // 流入：收入(to_account_id) + 转账(to_account_id)
            BigDecimal inflow = transactions.stream()
                    .filter(t -> accountId.equals(t.getToAccountId()))
                    .filter(t -> TransactionType.INCOME.getCode().equals(t.getType())
                            || TransactionType.TRANSFER.getCode().equals(t.getType()))
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 流出：支出(from_account_id) + 转账(from_account_id)
            BigDecimal outflow = transactions.stream()
                    .filter(t -> accountId.equals(t.getFromAccountId()))
                    .filter(t -> TransactionType.EXPENSE.getCode().equals(t.getType())
                            || TransactionType.TRANSFER.getCode().equals(t.getType()))
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal netFlow = inflow.subtract(outflow);
            BigDecimal endBalance = account.getCurrentBalance();
            BigDecimal beginBalance = endBalance.subtract(netFlow);

            flow.setBeginBalance(beginBalance);
            flow.setInflow(inflow);
            flow.setOutflow(outflow);
            flow.setNetFlow(netFlow);
            flow.setEndBalance(endBalance);

            accountFlows.add(flow);
            totalInflow = totalInflow.add(inflow);
            totalOutflow = totalOutflow.add(outflow);
        }

        vo.setTotalInflow(totalInflow);
        vo.setTotalOutflow(totalOutflow);
        vo.setNetFlow(totalInflow.subtract(totalOutflow));
        vo.setAccountFlows(accountFlows);
        return vo;
    }

}

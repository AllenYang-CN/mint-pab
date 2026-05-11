package com.mint.pab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mint.pab.dto.PageResult;
import com.mint.pab.dto.TransactionDTO;
import com.mint.pab.dto.TransactionQueryDTO;
import com.mint.pab.entity.Account;
import com.mint.pab.entity.Category;
import com.mint.pab.entity.Transaction;
import com.mint.pab.enums.AccountStatus;
import com.mint.pab.enums.TransactionType;
import com.mint.pab.exception.BusinessException;
import com.mint.pab.exception.ErrorCode;
import com.mint.pab.repository.AccountMapper;
import com.mint.pab.repository.CategoryMapper;
import com.mint.pab.repository.TransactionMapper;
import com.mint.pab.vo.TransactionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TransactionService {

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Transactional(rollbackFor = Exception.class)
    public TransactionVO createTransaction(Long userId, TransactionDTO dto) {
        // 1. 校验金额 > 0
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.TRANSACTION_AMOUNT_INVALID);
        }

        // 2. 校验交易类型枚举值合法性
        TransactionType transactionType = TransactionType.getByName(dto.getType());
        if (transactionType == null) {
            throw new BusinessException(ErrorCode.TRANSACTION_TYPE_INVALID);
        }

        // 3. 根据交易类型处理账户余额
        Integer typeCode = transactionType.getCode();
        if (TransactionType.INCOME.getCode().equals(typeCode)) {
            if (dto.getToAccountId() == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "收入交易必须选择收入账户");
            }
            Account account = accountMapper.selectByIdForUpdate(dto.getToAccountId());
            validateAccount(account);
            account.setCurrentBalance(account.getCurrentBalance().add(dto.getAmount()));
            account.setUpdateTime(LocalDateTime.now());
            accountMapper.updateById(account);
        } else if (TransactionType.EXPENSE.getCode().equals(typeCode)) {
            if (dto.getFromAccountId() == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "支出交易必须选择支出账户");
            }
            Account account = accountMapper.selectByIdForUpdate(dto.getFromAccountId());
            validateAccount(account);
            account.setCurrentBalance(account.getCurrentBalance().subtract(dto.getAmount()));
            account.setUpdateTime(LocalDateTime.now());
            accountMapper.updateById(account);
        } else if (TransactionType.TRANSFER.getCode().equals(typeCode)) {
            if (dto.getFromAccountId() == null || dto.getToAccountId() == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "转账交易必须选择转出和转入账户");
            }
            if (dto.getFromAccountId().equals(dto.getToAccountId())) {
                throw new BusinessException(ErrorCode.SAME_ACCOUNT_TRANSFER);
            }
            Account fromAccount;
            Account toAccount;
            // 按账户ID从小到大加锁，避免死锁
            if (dto.getFromAccountId() < dto.getToAccountId()) {
                fromAccount = accountMapper.selectByIdForUpdate(dto.getFromAccountId());
                toAccount = accountMapper.selectByIdForUpdate(dto.getToAccountId());
            } else {
                toAccount = accountMapper.selectByIdForUpdate(dto.getToAccountId());
                fromAccount = accountMapper.selectByIdForUpdate(dto.getFromAccountId());
            }
            validateAccount(fromAccount);
            validateAccount(toAccount);
            fromAccount.setCurrentBalance(fromAccount.getCurrentBalance().subtract(dto.getAmount()));
            fromAccount.setUpdateTime(LocalDateTime.now());
            toAccount.setCurrentBalance(toAccount.getCurrentBalance().add(dto.getAmount()));
            toAccount.setUpdateTime(LocalDateTime.now());
            accountMapper.updateById(fromAccount);
            accountMapper.updateById(toAccount);
        } else {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无效的交易类型");
        }

        // 4. 校验 categoryId 存在且为二级分类（叶节点）
        Category category = categoryMapper.selectById(dto.getCategoryId());
        if (category == null || category.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "分类不存在");
        }
        if (category.getParentId() == null) {
            throw new BusinessException(ErrorCode.CATEGORY_IS_LEAF_ONLY);
        }

        // 4. 解析 transactionTime
        LocalDateTime transactionTime = LocalDateTime.parse(dto.getTransactionTime(), DATE_TIME_FORMATTER);

        // 5. 构建 Transaction 实体，插入数据库
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setType(typeCode);
        transaction.setFromAccountId(dto.getFromAccountId());
        transaction.setToAccountId(dto.getToAccountId());
        transaction.setAmount(dto.getAmount());
        transaction.setCategoryId(dto.getCategoryId());
        transaction.setTransactionTime(transactionTime);
        transaction.setRemark(dto.getRemark());
        transaction.setCreateTime(LocalDateTime.now());
        transaction.setUpdateTime(LocalDateTime.now());
        transactionMapper.insert(transaction);

        // 6. 构建 TransactionVO 返回
        return buildTransactionVO(transaction);
    }

    @Transactional(rollbackFor = Exception.class)
    public TransactionVO updateTransaction(Long userId, Long id, TransactionDTO dto) {
        // 1. 查询原交易，校验归属权
        Transaction original = transactionMapper.selectById(id);
        if (original == null || original.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "交易记录不存在");
        }
        if (!userId.equals(original.getUserId())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无权操作该交易记录");
        }

        // 2. 校验交易类型枚举值合法性
        TransactionType transactionType = TransactionType.getByName(dto.getType());
        if (transactionType == null) {
            throw new BusinessException(ErrorCode.TRANSACTION_TYPE_INVALID);
        }

        // 3. 不允许修改交易类型
        if (!original.getType().equals(transactionType.getCode())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不允许修改交易类型");
        }

        // 4. 回滚原交易影响
        rollbackTransaction(original);

        // 5. 应用新交易影响（同createTransaction逻辑）
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.TRANSACTION_AMOUNT_INVALID);
        }

        Integer typeCode = transactionType.getCode();
        if (TransactionType.INCOME.getCode().equals(typeCode)) {
            if (dto.getToAccountId() == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "收入交易必须选择收入账户");
            }
            Account account = accountMapper.selectByIdForUpdate(dto.getToAccountId());
            validateAccount(account);
            account.setCurrentBalance(account.getCurrentBalance().add(dto.getAmount()));
            account.setUpdateTime(LocalDateTime.now());
            accountMapper.updateById(account);
        } else if (TransactionType.EXPENSE.getCode().equals(typeCode)) {
            if (dto.getFromAccountId() == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "支出交易必须选择支出账户");
            }
            Account account = accountMapper.selectByIdForUpdate(dto.getFromAccountId());
            validateAccount(account);
            account.setCurrentBalance(account.getCurrentBalance().subtract(dto.getAmount()));
            account.setUpdateTime(LocalDateTime.now());
            accountMapper.updateById(account);
        } else if (TransactionType.TRANSFER.getCode().equals(typeCode)) {
            if (dto.getFromAccountId() == null || dto.getToAccountId() == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "转账交易必须选择转出和转入账户");
            }
            if (dto.getFromAccountId().equals(dto.getToAccountId())) {
                throw new BusinessException(ErrorCode.SAME_ACCOUNT_TRANSFER);
            }
            Account fromAccount;
            Account toAccount;
            if (dto.getFromAccountId() < dto.getToAccountId()) {
                fromAccount = accountMapper.selectByIdForUpdate(dto.getFromAccountId());
                toAccount = accountMapper.selectByIdForUpdate(dto.getToAccountId());
            } else {
                toAccount = accountMapper.selectByIdForUpdate(dto.getToAccountId());
                fromAccount = accountMapper.selectByIdForUpdate(dto.getFromAccountId());
            }
            validateAccount(fromAccount);
            validateAccount(toAccount);
            fromAccount.setCurrentBalance(fromAccount.getCurrentBalance().subtract(dto.getAmount()));
            fromAccount.setUpdateTime(LocalDateTime.now());
            toAccount.setCurrentBalance(toAccount.getCurrentBalance().add(dto.getAmount()));
            toAccount.setUpdateTime(LocalDateTime.now());
            accountMapper.updateById(fromAccount);
            accountMapper.updateById(toAccount);
        }

        // 5. 校验 categoryId 存在且为二级分类（叶节点）
        Category category = categoryMapper.selectById(dto.getCategoryId());
        if (category == null || category.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "分类不存在");
        }
        if (category.getParentId() == null) {
            throw new BusinessException(ErrorCode.CATEGORY_IS_LEAF_ONLY);
        }

        // 6. 解析 transactionTime
        LocalDateTime transactionTime = LocalDateTime.parse(dto.getTransactionTime(), DATE_TIME_FORMATTER);

        // 7. 更新 Transaction 记录
        original.setFromAccountId(dto.getFromAccountId());
        original.setToAccountId(dto.getToAccountId());
        original.setAmount(dto.getAmount());
        original.setCategoryId(dto.getCategoryId());
        original.setTransactionTime(transactionTime);
        original.setRemark(dto.getRemark());
        original.setUpdateTime(LocalDateTime.now());
        transactionMapper.updateById(original);

        // 8. 返回 TransactionVO
        return buildTransactionVO(original);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteTransaction(Long userId, Long id) {
        // 1. 查询交易，校验归属权
        Transaction transaction = transactionMapper.selectById(id);
        if (transaction == null || transaction.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "交易记录不存在");
        }
        if (!userId.equals(transaction.getUserId())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无权操作该交易记录");
        }

        // 2. 回滚余额
        rollbackTransaction(transaction);

        // 3. 逻辑删除交易记录
        transactionMapper.deleteById(id);
    }

    public TransactionVO getTransaction(Long userId, Long id) {
        // 1. 查询交易，校验归属权
        Transaction transaction = transactionMapper.selectById(id);
        if (transaction == null || transaction.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "交易记录不存在");
        }
        if (!userId.equals(transaction.getUserId())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "无权操作该交易记录");
        }

        // 2. 组装 TransactionVO
        return buildTransactionVO(transaction);
    }

    private TransactionVO buildTransactionVO(Transaction tx) {
        TransactionVO vo = new TransactionVO();
        vo.setId(tx.getId());
        // 转换 type: Integer code -> String 枚举名
        TransactionType typeEnum = TransactionType.getByCode(tx.getType());
        vo.setType(typeEnum != null ? typeEnum.name() : null);
        vo.setTypeName(typeEnum != null ? typeEnum.getDesc() : null);
        vo.setFromAccountId(tx.getFromAccountId());
        vo.setToAccountId(tx.getToAccountId());
        vo.setAmount(tx.getAmount());
        vo.setCategoryId(tx.getCategoryId());
        vo.setRemark(tx.getRemark());
        if (tx.getTransactionTime() != null) {
            vo.setTransactionTime(tx.getTransactionTime().format(DATE_TIME_FORMATTER));
        }
        if (tx.getCreateTime() != null) {
            vo.setCreateTime(tx.getCreateTime().format(DATE_TIME_FORMATTER));
        }

        if (tx.getFromAccountId() != null) {
            Account fromAccount = accountMapper.selectById(tx.getFromAccountId());
            if (fromAccount != null) {
                vo.setFromAccountName(fromAccount.getName());
            }
        }
        if (tx.getToAccountId() != null) {
            Account toAccount = accountMapper.selectById(tx.getToAccountId());
            if (toAccount != null) {
                vo.setToAccountName(toAccount.getName());
            }
        }
        if (tx.getCategoryId() != null) {
            Category category = categoryMapper.selectById(tx.getCategoryId());
            if (category != null) {
                if (category.getParentId() != null) {
                    Category parent = categoryMapper.selectById(category.getParentId());
                    vo.setCategoryParentName(parent != null ? parent.getName() : null);
                }
                vo.setCategoryName(category.getName());
            }
        }

        return vo;
    }

    private void rollbackTransaction(Transaction tx) {
        Integer type = tx.getType();
        if (TransactionType.INCOME.getCode().equals(type)) {
            if (tx.getToAccountId() != null) {
                Account account = accountMapper.selectByIdForUpdate(tx.getToAccountId());
                if (account != null) {
                    account.setCurrentBalance(account.getCurrentBalance().subtract(tx.getAmount()));
                    account.setUpdateTime(LocalDateTime.now());
                    accountMapper.updateById(account);
                }
            }
        } else if (TransactionType.EXPENSE.getCode().equals(type)) {
            if (tx.getFromAccountId() != null) {
                Account account = accountMapper.selectByIdForUpdate(tx.getFromAccountId());
                if (account != null) {
                    account.setCurrentBalance(account.getCurrentBalance().add(tx.getAmount()));
                    account.setUpdateTime(LocalDateTime.now());
                    accountMapper.updateById(account);
                }
            }
        } else if (TransactionType.TRANSFER.getCode().equals(type)) {
            if (tx.getFromAccountId() != null && tx.getToAccountId() != null) {
                Account fromAccount;
                Account toAccount;
                if (tx.getFromAccountId() < tx.getToAccountId()) {
                    fromAccount = accountMapper.selectByIdForUpdate(tx.getFromAccountId());
                    toAccount = accountMapper.selectByIdForUpdate(tx.getToAccountId());
                } else {
                    toAccount = accountMapper.selectByIdForUpdate(tx.getToAccountId());
                    fromAccount = accountMapper.selectByIdForUpdate(tx.getFromAccountId());
                }
                if (fromAccount != null) {
                    fromAccount.setCurrentBalance(fromAccount.getCurrentBalance().add(tx.getAmount()));
                    fromAccount.setUpdateTime(LocalDateTime.now());
                    accountMapper.updateById(fromAccount);
                }
                if (toAccount != null) {
                    toAccount.setCurrentBalance(toAccount.getCurrentBalance().subtract(tx.getAmount()));
                    toAccount.setUpdateTime(LocalDateTime.now());
                    accountMapper.updateById(toAccount);
                }
            }
        }
    }

    public PageResult<TransactionVO> queryTransactions(Long userId, TransactionQueryDTO query) {
        LambdaQueryWrapper<Transaction> wrapper = buildQueryWrapper(userId, query);

        Page<Transaction> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<Transaction> resultPage = transactionMapper.selectPage(page, wrapper);

        List<TransactionVO> voList = resultPage.getRecords().stream()
                .map(this::buildTransactionVO)
                .collect(Collectors.toList());

        PageResult<TransactionVO> pageResult = new PageResult<>();
        pageResult.setList(voList);
        pageResult.setPageNum((int) resultPage.getCurrent());
        pageResult.setPageSize((int) resultPage.getSize());
        pageResult.setTotal(resultPage.getTotal());
        pageResult.setPages((int) resultPage.getPages());
        return pageResult;
    }

    public List<TransactionVO> queryAllTransactions(Long userId, TransactionQueryDTO query) {
        LambdaQueryWrapper<Transaction> wrapper = buildQueryWrapper(userId, query);
        List<Transaction> list = transactionMapper.selectList(wrapper);
        return list.stream()
                .map(this::buildTransactionVO)
                .collect(Collectors.toList());
    }

    private LambdaQueryWrapper<Transaction> buildQueryWrapper(Long userId, TransactionQueryDTO query) {
        LambdaQueryWrapper<Transaction> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Transaction::getUserId, userId)
                .eq(Transaction::getIsDeleted, 0);

        // 处理交易类型：逗号分隔的多值，如 "INCOME,EXPENSE"
        if (StringUtils.isNotBlank(query.getTypes())) {
            List<Integer> typeCodes = parseTypeCodes(query.getTypes());
            if (!typeCodes.isEmpty()) {
                wrapper.in(Transaction::getType, typeCodes);
            }
        }

        // 处理日期范围：前端传 yyyy-MM-dd 格式
        if (StringUtils.isNotBlank(query.getStartDate())) {
            LocalDate startDate = LocalDate.parse(query.getStartDate(), DATE_FORMATTER);
            wrapper.ge(Transaction::getTransactionTime, startDate.atStartOfDay());
        }
        if (StringUtils.isNotBlank(query.getEndDate())) {
            LocalDate endDate = LocalDate.parse(query.getEndDate(), DATE_FORMATTER);
            wrapper.le(Transaction::getTransactionTime, endDate.atTime(LocalTime.MAX));
        }

        if (query.getMinAmount() != null) {
            wrapper.ge(Transaction::getAmount, query.getMinAmount());
        }
        if (query.getMaxAmount() != null) {
            wrapper.le(Transaction::getAmount, query.getMaxAmount());
        }

        // 处理账户ID：逗号分隔的多值，如 "1,2,3"，匹配转出或转入账户
        if (StringUtils.isNotBlank(query.getAccountIds())) {
            List<Long> ids = parseLongIds(query.getAccountIds());
            if (!ids.isEmpty()) {
                wrapper.and(w -> w.in(Transaction::getFromAccountId, ids)
                        .or()
                        .in(Transaction::getToAccountId, ids));
            }
        }

        // 处理分类ID：逗号分隔的多值，如 "4,5,6"
        if (StringUtils.isNotBlank(query.getCategoryIds())) {
            List<Long> ids = parseLongIds(query.getCategoryIds());
            if (!ids.isEmpty()) {
                wrapper.in(Transaction::getCategoryId, ids);
            }
        }

        if (StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.like(Transaction::getRemark, query.getKeyword());
        }

        wrapper.orderByDesc(Transaction::getTransactionTime);
        return wrapper;
    }

    private List<Integer> parseTypeCodes(String types) {
        if (StringUtils.isBlank(types)) {
            return Collections.emptyList();
        }
        return Arrays.stream(types.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(TransactionType::getByName)
                .filter(type -> type != null)
                .map(TransactionType::getCode)
                .collect(Collectors.toList());
    }

    private List<Long> parseLongIds(String ids) {
        if (StringUtils.isBlank(ids)) {
            return Collections.emptyList();
        }
        return Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    private void validateAccount(Account account) {
        if (account == null || account.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账户不存在");
        }
        if (!AccountStatus.ACTIVE.getCode().equals(account.getStatus())) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        }
    }
}

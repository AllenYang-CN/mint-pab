package com.mint.pab.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mint.pab.dto.AccountDTO;
import com.mint.pab.entity.Account;
import com.mint.pab.entity.Transaction;
import com.mint.pab.enums.AccountStatus;
import com.mint.pab.enums.AccountType;
import com.mint.pab.exception.BusinessException;
import com.mint.pab.exception.ErrorCode;
import com.mint.pab.repository.AccountMapper;
import com.mint.pab.repository.TransactionMapper;
import com.mint.pab.vo.AccountVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AccountService {

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    public List<AccountVO> getAccounts(Long userId) {
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Account::getUserId, userId)
                .orderByAsc(Account::getCreateTime);
        List<Account> accounts = accountMapper.selectList(wrapper);
        return accounts.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    public AccountVO createAccount(Long userId, AccountDTO dto) {
        // 1. 校验账户类型枚举值合法性
        AccountType accountType = AccountType.getByName(dto.getType());
        if (accountType == null) {
            throw new BusinessException(ErrorCode.ACCOUNT_TYPE_INVALID);
        }

        // 2. 校验同用户下名称唯一
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Account::getUserId, userId)
                .eq(Account::getName, dto.getName());
        Long count = accountMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_NAME_DUPLICATE);
        }

        // 3. 创建 Account 实体
        Account account = new Account();
        account.setUserId(userId);
        account.setName(dto.getName());
        account.setType(accountType.getCode());
        account.setInitialBalance(dto.getInitialBalance());
        account.setCurrentBalance(dto.getInitialBalance());
        account.setRemark(dto.getRemark());
        account.setStatus(AccountStatus.ACTIVE.getCode());
        account.setCreateTime(LocalDateTime.now());
        account.setUpdateTime(LocalDateTime.now());

        // 4. 插入数据库
        accountMapper.insert(account);
        return convertToVO(account);
    }

    public AccountVO updateAccount(Long userId, Long id, AccountDTO dto) {
        // 1. 查询账户，校验归属权
        Account account = accountMapper.selectById(id);
        if (account == null || !userId.equals(account.getUserId())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账户不存在或无权限");
        }

        // 2. 校验名称唯一（排除自身）
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Account::getUserId, userId)
                .eq(Account::getName, dto.getName())
                .ne(Account::getId, id);
        Long count = accountMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_NAME_DUPLICATE);
        }

        // 3. 只允许修改 name 和 remark
        account.setName(dto.getName());
        account.setRemark(dto.getRemark());
        account.setUpdateTime(LocalDateTime.now());

        // 4. 更新数据库
        accountMapper.updateById(account);
        return convertToVO(account);
    }

    public void deleteAccount(Long userId, Long id) {
        // 1. 查询账户，校验归属权
        Account account = accountMapper.selectById(id);
        if (account == null || !userId.equals(account.getUserId())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账户不存在或无权限");
        }

        // 2. 检查是否有关联交易
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getFromAccountId, id)
                .or()
                .eq(Transaction::getToAccountId, id);
        Long count = transactionMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_HAS_TRANSACTION);
        }

        // 3. 逻辑删除
        accountMapper.deleteById(id);
    }

    public void updateStatus(Long userId, Long id, String statusName) {
        // 1. 校验状态枚举值合法性
        AccountStatus accountStatus = AccountStatus.getByName(statusName);
        if (accountStatus == null) {
            throw new BusinessException(ErrorCode.ACCOUNT_STATUS_INVALID);
        }

        // 2. 查询账户，校验归属权
        Account account = accountMapper.selectById(id);
        if (account == null || !userId.equals(account.getUserId())) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账户不存在或无权限");
        }

        // 3. 更新 status 字段
        account.setStatus(accountStatus.getCode());
        account.setUpdateTime(LocalDateTime.now());
        accountMapper.updateById(account);
    }

    /**
     * 将 Account 实体转换为 AccountVO，将 Integer 类型的 type/status 转换为 String 枚举名
     */
    private AccountVO convertToVO(Account account) {
        AccountVO vo = new AccountVO();
        vo.setId(account.getId());
        vo.setName(account.getName());

        // 转换 type: Integer -> String 枚举名
        AccountType typeEnum = AccountType.getByCode(account.getType());
        vo.setType(typeEnum != null ? typeEnum.name() : null);
        vo.setTypeName(typeEnum != null ? typeEnum.getDesc() : "未知");

        vo.setInitialBalance(account.getInitialBalance());
        vo.setCurrentBalance(account.getCurrentBalance());
        vo.setRemark(account.getRemark());

        // 转换 status: Integer -> String 枚举名
        AccountStatus statusEnum = AccountStatus.getByCode(account.getStatus());
        vo.setStatus(statusEnum != null ? statusEnum.name() : null);
        vo.setStatusName(statusEnum != null ? statusEnum.getDesc() : "未知");

        vo.setCreateTime(account.getCreateTime());
        vo.setUpdateTime(account.getUpdateTime());
        return vo;
    }
}

package com.mint.pab.controller;

import com.mint.pab.dto.AccountDTO;
import com.mint.pab.dto.Result;
import com.mint.pab.service.AccountService;
import com.mint.pab.vo.AccountVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping
    public Result<List<AccountVO>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<AccountVO> accounts = accountService.getAccounts(userId);
        return Result.success(accounts);
    }

    @PostMapping
    public Result<AccountVO> create(@Valid @RequestBody AccountDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        AccountVO account = accountService.createAccount(userId, dto);
        return Result.success(account);
    }

    @PutMapping("/{id}")
    public Result<AccountVO> update(@PathVariable Long id, @Valid @RequestBody AccountDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        AccountVO account = accountService.updateAccount(userId, id, dto);
        return Result.success(account);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        accountService.deleteAccount(userId, id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String status = body.get("status");
        accountService.updateStatus(userId, id, status);
        return Result.success();
    }
}

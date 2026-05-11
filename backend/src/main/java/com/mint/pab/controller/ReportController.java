package com.mint.pab.controller;

import com.mint.pab.dto.Result;
import com.mint.pab.service.ReportService;
import com.mint.pab.vo.BalanceSheetVO;
import com.mint.pab.vo.CashFlowVO;
import com.mint.pab.vo.IncomeExpenseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/balance-sheet")
    public Result<BalanceSheetVO> balanceSheet(@RequestParam String date, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        BalanceSheetVO vo = reportService.getBalanceSheet(userId, date);
        return Result.success(vo);
    }

    @GetMapping("/income-expense")
    public Result<IncomeExpenseVO> incomeExpense(@RequestParam String month, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        IncomeExpenseVO vo = reportService.getIncomeExpense(userId, month);
        return Result.success(vo);
    }

    @GetMapping("/cash-flow")
    public Result<CashFlowVO> cashFlow(@RequestParam String month, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        CashFlowVO vo = reportService.getCashFlow(userId, month);
        return Result.success(vo);
    }

}

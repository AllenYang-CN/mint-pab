package com.mint.pab.controller;

import com.mint.pab.dto.BudgetDTO;
import com.mint.pab.dto.Result;
import com.mint.pab.service.BudgetService;
import com.mint.pab.vo.BudgetExecutionVO;
import com.mint.pab.vo.BudgetVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @GetMapping
    public Result<List<BudgetVO>> list(@RequestParam String month, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<BudgetVO> budgets = budgetService.getBudgets(userId, month);
        return Result.success(budgets);
    }

    @PostMapping
    public Result<BudgetVO> create(@Valid @RequestBody BudgetDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        BudgetVO budget = budgetService.createBudget(userId, dto);
        return Result.success(budget);
    }

    @PutMapping("/{id}")
    public Result<BudgetVO> update(@PathVariable Long id, @Valid @RequestBody BudgetDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        BudgetVO budget = budgetService.updateBudget(userId, id, dto);
        return Result.success(budget);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        budgetService.deleteBudget(userId, id);
        return Result.success();
    }

    @GetMapping("/execution")
    public Result<BudgetExecutionVO> execution(@RequestParam String month, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        BudgetExecutionVO vo = budgetService.getBudgetExecution(userId, month);
        return Result.success(vo);
    }

}

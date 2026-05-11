package com.mint.pab.controller;

import com.mint.pab.dto.CategoryDTO;
import com.mint.pab.dto.Result;
import com.mint.pab.service.CategoryService;
import com.mint.pab.vo.CategoryTreeVO;
import com.mint.pab.vo.CategoryVO;
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
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public Result<List<CategoryVO>> list(@RequestParam(required = false) String type, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<CategoryVO> categories = categoryService.getCategories(userId, type);
        return Result.success(categories);
    }

    @GetMapping("/tree")
    public Result<List<CategoryTreeVO>> tree(@RequestParam(required = false) String type, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<CategoryTreeVO> tree = categoryService.getCategoryTree(userId, type);
        return Result.success(tree);
    }

    @PostMapping
    public Result<CategoryVO> create(@Valid @RequestBody CategoryDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        CategoryVO category = categoryService.createCategory(userId, dto);
        return Result.success(category);
    }

    @PutMapping("/{id}")
    public Result<CategoryVO> update(@PathVariable Long id, @Valid @RequestBody CategoryDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        CategoryVO category = categoryService.updateCategory(userId, id, dto);
        return Result.success(category);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        categoryService.deleteCategory(userId, id);
        return Result.success();
    }
}

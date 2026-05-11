package com.mint.pab.controller;

import com.mint.pab.dto.Result;
import com.mint.pab.service.DashboardService;
import com.mint.pab.vo.DashboardVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/summary")
    public Result<DashboardVO> summary(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        DashboardVO vo = dashboardService.getSummary(userId);
        return Result.success(vo);
    }

}

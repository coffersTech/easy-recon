package com.coffers.easy.recon.demo.controller;

import org.springframework.web.bind.annotation.*;
import tech.coffers.recon.api.EasyReconApi;
import tech.coffers.recon.api.enums.ReconStatusEnum;
import tech.coffers.recon.api.result.*;
import com.coffers.easy.recon.demo.service.DemoService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 对账数据可视化接口
 */
@RestController
@RequestMapping("/api/recon")
@CrossOrigin // 支持开发环境调试
public class ReconDataController {

    private final EasyReconApi easyReconApi;
    private final DemoService demoService;

    public ReconDataController(EasyReconApi easyReconApi, DemoService demoService) {
        this.easyReconApi = easyReconApi;
        this.demoService = demoService;
    }

    /**
     * 生成演示数据 (供前端触发)
     */
    @PostMapping("/generate-demo-data")
    public void generateDemoData(
            @RequestBody(required = false) com.coffers.easy.recon.demo.model.DemoGenRequest request) {
        if (request == null || request.getType() == null || request.getType().isEmpty()) {
            demoService.runAllScenarios();
        } else {
            demoService.runScenario(request);
        }
    }

    /**
     * 今日对账统计汇总
     */
    @GetMapping("/stats")
    public Map<String, Object> getTodayStats() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, Object> stats = new HashMap<>();

        // 使用 SDK 现有的 getReconSummary 获取汇总信息
        ReconSummaryResult summary = easyReconApi.getReconSummary(today);

        if (summary != null) {
            stats.put("total", summary.getTotalOrders());
            stats.put("success", summary.getSuccessCount());
            stats.put("failure", summary.getFailCount());
            stats.put("pending", summary.getInitCount());
            stats.put("amount", summary.getTotalAmount());
        } else {
            // 如果当天还没有汇总记录，则补 0
            stats.put("total", 0);
            stats.put("success", 0);
            stats.put("failure", 0);
            stats.put("pending", 0);
            stats.put("amount", 0);
        }

        // 异常记录数 (今日)
        PageResult<ReconExceptionResult> exceptionsPage = easyReconApi.listExceptions(null, today, today, null, 1, 1);
        stats.put("exceptions", exceptionsPage.getTotal());
        stats.put("date", today);

        return stats;
    }

    /**
     * 订单分页列表
     */
    @GetMapping("/orders")
    public PageResult<ReconOrderMainResult> getOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Integer reconStatus) {

        String dateStr = (date != null && !date.isEmpty()) ? date
                : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        ReconStatusEnum statusEnum = reconStatus != null ? ReconStatusEnum.fromCode(reconStatus) : null;

        return easyReconApi.listOrdersByDate(dateStr, statusEnum, page, size);
    }

    /**
     * 订单三级数据透视详情
     */
    @GetMapping("/orders/{orderNo}/detail")
    public Map<String, Object> getOrderDetail(@PathVariable String orderNo) {
        Map<String, Object> detail = new HashMap<>();

        ReconOrderMainResult main = easyReconApi.getOrderMain(orderNo);
        List<ReconOrderSubResult> subs = easyReconApi.getOrderSubs(orderNo);
        List<ReconOrderSplitDetailResult> splitFacts = easyReconApi.getSplitDetails(orderNo);
        List<ReconExceptionResult> exceptions = easyReconApi.getReconExceptions(orderNo);

        detail.put("main", main);
        detail.put("subs", subs);
        detail.put("splitFacts", splitFacts);
        detail.put("exceptions", exceptions);

        return detail;
    }

    /**
     * 最近异常记录
     */
    @GetMapping("/recent-exceptions")
    public List<ReconExceptionResult> getRecentExceptions() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // 获取今日异常记录第一页
        PageResult<ReconExceptionResult> page = easyReconApi.listExceptions(null, today, today, null, 1, 10);
        return page.getList();
    }
}

package com.knowledge.base.statistics.controller;

import com.knowledge.base.common.result.Result;
import com.knowledge.base.statistics.service.StatisticsExportService;
import com.knowledge.base.statistics.service.StatisticsService;
import com.knowledge.base.statistics.vo.StatisticsVOs.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class StatisticsController {
    private final StatisticsService statisticsService;
    private final StatisticsExportService exportService;
    @GetMapping("/overview") public Result<Overview> overview(@RequestParam(required=false) LocalDate startDate,@RequestParam(required=false) LocalDate endDate){return Result.success(statisticsService.getOverview(startDate,endDate));}
    @GetMapping("/admin-overview") public Result<Overview> adminOverview(){return Result.success(statisticsService.getOverview(null,null));}
    @GetMapping("/dashboard") public Result<Dashboard> dashboard(){return Result.success(statisticsService.getDashboard());}
    @GetMapping("/trend/document") public Result<List<Trend>> trend(@RequestParam LocalDate startDate,@RequestParam LocalDate endDate,@RequestParam(defaultValue="create") String type){return Result.success(statisticsService.getDocumentTrend(startDate,endDate,type));}
    @GetMapping("/activity/user") public Result<List<Activity>> activity(@RequestParam LocalDate startDate,@RequestParam LocalDate endDate){return Result.success(statisticsService.getUserActivity(startDate,endDate));}
    @GetMapping("/distribution/category") public Result<List<Category>> categories(){return Result.success(statisticsService.getCategoryDistribution());}
    @GetMapping("/hot/document") public Result<List<HotDocument>> hot(@RequestParam(defaultValue="view") String type,@RequestParam(defaultValue="10") Integer size){return Result.success(statisticsService.getHotDocuments(type,size));}
    @GetMapping("/active/user") public Result<List<ActiveUser>> active(@RequestParam(defaultValue="create") String type,@RequestParam(defaultValue="10") Integer size){return Result.success(statisticsService.getActiveUsers(type,size));}
    @DeleteMapping("/cache") public Result<String> clearCache(){statisticsService.clearAllCache();return Result.success("Statistics cache cleared");}
    @PostMapping("/aggregation/document") public Result<String> aggregateDocument(){statisticsService.triggerDocumentAggregation();return Result.success("Document aggregation triggered");}
    @PostMapping("/aggregation/user") public Result<String> aggregateUser(){statisticsService.triggerUserAggregation();return Result.success("User aggregation triggered");}
    @GetMapping("/export/trend") public void exportTrend(@RequestParam LocalDate startDate,@RequestParam LocalDate endDate,@RequestParam(defaultValue="create") String type,HttpServletResponse response)throws IOException{exportService.exportDocumentTrend(startDate,endDate,type,response);}
    @GetMapping("/export/hot-documents") public void exportHot(@RequestParam(defaultValue="view") String type,@RequestParam(defaultValue="20") Integer size,HttpServletResponse response)throws IOException{exportService.exportHotDocuments(type,size,response);}
    @GetMapping("/export/active-users") public void exportActive(@RequestParam(defaultValue="create") String type,@RequestParam(defaultValue="20") Integer size,HttpServletResponse response)throws IOException{exportService.exportActiveUsers(type,size,response);}
}

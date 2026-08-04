package com.knowledge.base.statistics.service.impl;

import com.knowledge.base.statistics.service.StatisticsExportService;
import com.knowledge.base.statistics.service.StatisticsService;
import com.knowledge.base.statistics.vo.StatisticsVOs.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StatisticsExportServiceImpl implements StatisticsExportService {
    private final StatisticsService statisticsService;
    @Override public void exportDocumentTrend(LocalDate start, LocalDate end, String type, HttpServletResponse response) throws IOException { response(response, "document-trend-" + start + "-" + end + ".csv"); try(PrintWriter w=writer(response)){w.println("date,count"); for(Trend v:statisticsService.getDocumentTrend(start,end,type))w.println(csv(v.getDate())+","+v.getCount());} }
    @Override public void exportHotDocuments(String type,Integer size,HttpServletResponse response)throws IOException{response(response,"hot-documents-"+LocalDate.now()+".csv");try(PrintWriter w=writer(response)){w.println("rank,documentId,title,viewCount,likeCount,favoriteCount,commentCount,statisticsValue");int i=1;for(HotDocument v:statisticsService.getHotDocuments(type,size))w.println(i+++","+v.getDocumentId()+","+csv(v.getTitle())+","+v.getViewCount()+","+v.getLikeCount()+","+v.getFavoriteCount()+","+v.getCommentCount()+","+v.getStatisticsValue());}}
    @Override public void exportActiveUsers(String type,Integer size,HttpServletResponse response)throws IOException{response(response,"active-users-"+LocalDate.now()+".csv");try(PrintWriter w=writer(response)){w.println("rank,userId,username,realName,documentCount,commentCount,viewCount,statisticsValue");int i=1;for(ActiveUser v:statisticsService.getActiveUsers(type,size))w.println(i+++","+v.getUserId()+","+csv(v.getUsername())+","+csv(v.getRealName())+","+v.getDocumentCount()+","+v.getCommentCount()+","+v.getViewCount()+","+v.getStatisticsValue());}}
    private PrintWriter writer(HttpServletResponse r)throws IOException{PrintWriter w=new PrintWriter(new OutputStreamWriter(r.getOutputStream(),StandardCharsets.UTF_8));w.write('\uFEFF');return w;}
    private void response(HttpServletResponse r,String name){r.setContentType("text/csv;charset=UTF-8");r.setCharacterEncoding("UTF-8");r.setHeader("Content-Disposition","attachment; filename*=UTF-8''"+URLEncoder.encode(name,StandardCharsets.UTF_8));}
    private String csv(String value){if(value==null)return "";return value.contains(",")||value.contains("\"")||value.contains("\n")?"\""+value.replace("\"","\"\"")+"\"":value;}
}

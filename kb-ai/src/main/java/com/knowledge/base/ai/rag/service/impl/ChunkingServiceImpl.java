package com.knowledge.base.ai.rag.service.impl;
import com.knowledge.base.ai.rag.config.RagProperties;
import com.knowledge.base.ai.rag.entity.DocumentChunk;
import com.knowledge.base.ai.rag.service.ChunkingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.*;

@Service @RequiredArgsConstructor
public class ChunkingServiceImpl implements ChunkingService {
    private final RagProperties properties;
    private static final Pattern HEADING = Pattern.compile("^(#{1,6})\\s+(.+)$", Pattern.MULTILINE);
    @Override public List<DocumentChunk> chunk(String content, Long documentId, String title, Long categoryId, Long authorId, Long teamId, Integer status) {
        if (content == null || content.isBlank()) return List.of();
        int max = Math.max(100, properties.getChunking().getChunkSize() * 2), overlap = Math.max(0, properties.getChunking().getChunkOverlap() * 2);
        List<Part> sections = sections(content); List<DocumentChunk> result = new ArrayList<>();
        for (Part section : sections) for (String piece : pieces(section.content(), max, overlap)) result.add(DocumentChunk.builder().chunkId(UUID.randomUUID().toString()).documentId(documentId).documentTitle(title).content((section.heading()==null?"":section.heading()+"\n\n")+piece).heading(section.heading()).chunkIndex(result.size()).categoryId(categoryId).authorId(authorId).teamId(teamId).docStatus(status).indexedAt(LocalDateTime.now()).build());
        result.forEach(c -> c.setTotalChunks(result.size())); return result;
    }
    private List<Part> sections(String content) { List<Part> list=new ArrayList<>(); Matcher m=HEADING.matcher(content); int start=0; String heading=null; while(m.find()){ add(list,heading,content.substring(start,m.start())); heading=m.group(2); start=m.end(); } add(list,heading,content.substring(start)); if(list.isEmpty()) add(list,null,content); return list; }
    private void add(List<Part> list,String heading,String content){ if(!content.isBlank()) list.add(new Part(heading,content.trim())); }
    private List<String> pieces(String content,int max,int overlap){ List<String> out=new ArrayList<>(); int start=0; while(start<content.length()){ int end=Math.min(content.length(),start+max); if(end<content.length()){ int split=Math.max(content.lastIndexOf("\n\n",end),content.lastIndexOf('。',end)); if(split>start+max/2) end=split+1; } out.add(content.substring(start,end).trim()); if(end==content.length()) break; start=Math.max(end-overlap,start+1); } return out; }
    private record Part(String heading,String content) {}
}

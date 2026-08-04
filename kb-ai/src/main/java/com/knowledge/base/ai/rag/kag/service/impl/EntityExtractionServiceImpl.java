package com.knowledge.base.ai.rag.kag.service.impl;

import com.knowledge.base.ai.config.ModelProvider;
import com.knowledge.base.ai.rag.kag.dto.EntityMergeDTO;
import com.knowledge.base.ai.rag.kag.dto.ExtractionResult;
import com.knowledge.base.ai.rag.kag.dto.RelationMergeDTO;
import com.knowledge.base.ai.rag.kag.service.EntityExtractionService;
import dev.langchain4j.data.message.UserMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntityExtractionServiceImpl implements EntityExtractionService {
    private static final Pattern ENTITY_LINE = Pattern.compile("^ENTITY\\|([^|]+)\\|([^|]*)\\|?(.*)$");
    private static final Pattern RELATION_LINE = Pattern.compile("^RELATION\\|([^|]+)\\|([^|]+)\\|([^|]+)$");
    private static final Pattern TECHNICAL_TERM = Pattern.compile("[A-Za-z][A-Za-z0-9+#._-]{1,}");
    private final ModelProvider modelProvider;

    @Value("${kag.llm-extraction-enabled:true}")
    private boolean llmExtractionEnabled;

    @Override
    public ExtractionResult extract(String content, String heading) {
        if (content == null || content.isBlank()) return empty();
        if (llmExtractionEnabled) {
            try {
                ExtractionResult result = parse(modelProvider.getDefaultModel().generate(UserMessage.from(prompt(content, heading))).content().text());
                if (!result.getEntities().isEmpty()) return result;
            } catch (Exception exception) {
                log.warn("KAG LLM entity extraction failed; falling back to deterministic extraction: {}", exception.getMessage());
            }
        }
        return fallback(content, heading);
    }

    private ExtractionResult parse(String response) {
        Map<String, EntityMergeDTO> entities = new LinkedHashMap<>();
        List<RelationMergeDTO> relations = new ArrayList<>();
        for (String rawLine : response.split("\\R")) {
            String line = rawLine.trim();
            Matcher entity = ENTITY_LINE.matcher(line);
            if (entity.matches()) {
                EntityMergeDTO value = new EntityMergeDTO();
                value.setName(entity.group(1).trim());
                value.setType(blankTo(entity.group(2), "CONCEPT"));
                value.setDescription(entity.group(3).trim());
                if (!value.getName().isBlank()) entities.putIfAbsent(value.getName(), value);
                continue;
            }
            Matcher relation = RELATION_LINE.matcher(line);
            if (relation.matches()) {
                RelationMergeDTO value = new RelationMergeDTO();
                value.setSource(relation.group(1).trim());
                value.setRelationType(relation.group(2).trim().toUpperCase(Locale.ROOT));
                value.setTarget(relation.group(3).trim());
                value.setWeight(1.0D);
                if (!value.getSource().isBlank() && !value.getTarget().isBlank()) relations.add(value);
            }
        }
        ExtractionResult result = new ExtractionResult();
        result.setEntities(new ArrayList<>(entities.values()));
        result.setRelations(relations);
        return result;
    }

    private ExtractionResult fallback(String content, String heading) {
        Map<String, EntityMergeDTO> entities = new LinkedHashMap<>();
        if (heading != null && !heading.isBlank()) add(entities, heading.trim(), "CONCEPT", "Document heading");
        Matcher matcher = TECHNICAL_TERM.matcher(content);
        while (matcher.find() && entities.size() < 20) add(entities, matcher.group(), "TECH_STACK", "Extracted technical term");
        ExtractionResult result = new ExtractionResult();
        result.setEntities(new ArrayList<>(entities.values()));
        result.setRelations(List.of());
        return result;
    }

    private void add(Map<String, EntityMergeDTO> entities, String name, String type, String description) {
        if (name.length() < 2) return;
        EntityMergeDTO entity = new EntityMergeDTO();
        entity.setName(name); entity.setType(type); entity.setDescription(description); entity.setAliases(List.of());
        entities.putIfAbsent(name, entity);
    }

    private ExtractionResult empty() { ExtractionResult result = new ExtractionResult(); result.setEntities(List.of()); result.setRelations(List.of()); return result; }
    private String blankTo(String value, String fallback) { return value == null || value.isBlank() ? fallback : value.trim(); }
    private String prompt(String content, String heading) { return "Extract knowledge graph facts. Return only lines in these formats:\\nENTITY|name|type|description\\nRELATION|source|relation_type|target\\nTypes: TECH_STACK, API, CONFIG, CONCEPT, TOOL, PROCESS. Heading: " + (heading == null ? "" : heading) + "\\nContent:\\n" + content; }
}

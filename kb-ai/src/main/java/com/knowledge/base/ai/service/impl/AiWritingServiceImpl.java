package com.knowledge.base.ai.service.impl;

import com.knowledge.base.ai.config.ModelProvider;
import com.knowledge.base.ai.dto.WritingRequestDTO;
import com.knowledge.base.ai.service.AiWritingService;
import com.knowledge.base.ai.vo.WritingResultVO;
import com.knowledge.base.ai.vo.WritingTemplateVO;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiWritingServiceImpl implements AiWritingService {
    private final ModelProvider modelProvider;

    @Override
    public WritingResultVO generate(WritingRequestDTO dto, Long userId) {
        return callModel("生成", dto, buildWritingPrompt(dto));
    }

    @Override
    public SseEmitter generateStream(WritingRequestDTO dto, Long userId) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        CompletableFuture.runAsync(() -> {
            try {
                WritingResultVO result = generate(dto, userId);
                String content = result.getContent();
                for (int i = 0; i < content.length(); i += 10) {
                    emitter.send(SseEmitter.event().name("message").data(content.substring(i, Math.min(i + 10, content.length()))));
                }
                emitter.send(SseEmitter.event().name("done").data(result));
                emitter.complete();
            } catch (Exception e) {
                log.error("AI写作流式生成失败", e);
                try {
                    emitter.send(SseEmitter.event().name("error").data(e.getMessage() == null ? "AI写作生成失败" : e.getMessage()));
                } catch (IOException ignored) {
                    log.debug("SSE错误事件发送失败", ignored);
                }
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    @Override
    public WritingResultVO expand(WritingRequestDTO dto, Long userId) {
        requireExistingContent(dto);
        String prompt = "你是一位专业的文档写作助手。请对以下内容进行扩写，保持主题和风格，丰富细节、背景、论证和实例，保持结构清晰，直接输出扩写后的完整内容。\n\n原文主题：" + dto.getTopic() + "\n\n原文内容：\n" + dto.getExistingContent();
        return callModel("扩写", dto, prompt);
    }

    @Override
    public WritingResultVO optimize(WritingRequestDTO dto, Long userId) {
        requireExistingContent(dto);
        String prompt = "你是一位专业的文档编辑助手。请优化和润色以下内容，修正语法，改善句式与段落组织，保持核心意思不变。" + styleDescription(dto.getStyle()) + "\n\n原文标题：" + dto.getTopic() + "\n\n原文内容：\n" + dto.getExistingContent();
        return callModel("优化", dto, prompt);
    }

    @Override
    public WritingResultVO continueWriting(WritingRequestDTO dto, Long userId) {
        requireExistingContent(dto);
        String prompt = "你是一位专业的文档写作助手。请从以下内容的结尾继续写作，延续主题、风格和逻辑，不重复已有内容，直接输出续写内容。\n\n原文主题：" + dto.getTopic() + "\n\n已有内容：\n" + dto.getExistingContent();
        return callModel("续写", dto, prompt);
    }

    @Override
    @Cacheable("writingTemplates")
    public List<WritingTemplateVO> getTemplates() {
        List<WritingTemplateVO> templates = new ArrayList<>();
        templates.add(template("tech-solution", "技术方案", "适用于撰写技术方案文档", "技术文档", "请撰写一份技术方案，包含项目背景与目标、技术现状分析、方案设计、技术选型、实施计划、风险评估与应对。", "article", "technical"));
        templates.add(template("project-report", "项目报告", "适用于项目总结报告", "工作汇报", "请撰写一份项目总结报告，包含项目概述、执行过程、主要成果、问题与挑战、经验与反思、下一步计划。", "report", "formal"));
        templates.add(template("prd", "产品需求文档(PRD)", "适用于产品需求文档", "产品文档", "请撰写一份产品需求文档(PRD)，包含需求背景与目标、目标用户、核心功能、用户流程、交互要求、验收标准。", "documentation", "technical"));
        templates.add(template("api-doc", "API接口文档", "适用于编写API接口文档", "技术文档", "请撰写一份API接口文档，包含接口概述、认证方式、请求和响应格式、接口详情、示例及错误码。", "documentation", "technical"));
        templates.add(template("weekly-report", "周报/工作汇报", "适用于周报或工作汇报", "工作汇报", "请撰写一份工作周报，包含本周完成工作、重点进展、问题及解决方案、下周计划、需要协调事项。", "report", "formal"));
        templates.add(template("meeting-minutes", "会议纪要", "适用于会议纪要", "工作汇报", "请撰写一份会议纪要，包含会议基本信息、议题、讨论要点、决议、待办任务及负责人、下次会议时间。", "email", "formal"));
        templates.add(template("announcement", "公告通知", "适用于公司内部公告或通知", "行政文档", "请撰写一份公告通知，包含公告缘由、具体事项、执行要求或时间安排、联系方式。", "announcement", "formal"));
        templates.add(template("email-template", "邮件模板", "适用于编写工作邮件", "日常沟通", "请撰写一封工作邮件，包含清晰主题、称呼、事项说明、背景信息、行动要求和专业结尾。", "email", "formal"));
        return templates;
    }

    private WritingResultVO callModel(String action, WritingRequestDTO dto, String prompt) {
        String modelName = dto.getModel() == null || dto.getModel().isBlank() ? modelProvider.getDefaultModelName() : dto.getModel();
        ChatLanguageModel model = modelProvider.getModel(modelName);
        log.info("AI写作{}请求: model={}, topic={}, promptLength={}", action, modelName, dto.getTopic(), prompt.length());
        try {
            Response<AiMessage> response = model.generate(UserMessage.from(prompt));
            String content = response.content().text().trim();
            Integer tokens = response.tokenUsage() == null ? null : response.tokenUsage().totalTokenCount();
            return WritingResultVO.builder().content(content).tokens(tokens).wordCount(content.length()).model(modelName).build();
        } catch (Exception e) {
            throw new IllegalStateException("AI写作" + action + "失败: " + e.getMessage(), e);
        }
    }

    private String buildWritingPrompt(WritingRequestDTO dto) {
        String type = switch (valueOr(dto.getContentType(), "article")) {
            case "report" -> "报告，要求结构清晰、论证充分";
            case "documentation" -> "技术文档，要求准确、规范、可操作，可包含代码示例";
            case "email" -> "邮件，要求礼貌、简洁、重点突出";
            case "announcement" -> "公告，要求正式、严谨、表述清楚";
            default -> "文章，要求主题明确、逻辑清晰、表达流畅";
        };
        String style = switch (valueOr(dto.getStyle(), "formal")) {
            case "casual" -> "轻松随意";
            case "technical" -> "专业技术，注重准确性和严谨性";
            case "creative" -> "具有创造性";
            case "academic" -> "严谨的学术表达";
            default -> "正式规范";
        };
        int length = dto.getLength() != null && dto.getLength() > 0 ? dto.getLength() : 800;
        StringBuilder prompt = new StringBuilder("你是一位专业的文档写作助手。请写作一份").append(type).append("。\n");
        prompt.append("写作风格：").append(style).append("。\n");
        if (dto.getTone() != null && !dto.getTone().isBlank()) prompt.append("语气：").append(dto.getTone()).append("。\n");
        prompt.append("字数要求：约").append(length).append("字。\n\n写作主题：").append(dto.getTopic()).append('\n');
        if (dto.getRequirements() != null && !dto.getRequirements().isBlank()) prompt.append("写作要求：").append(dto.getRequirements()).append('\n');
        if (dto.getExistingContent() != null && !dto.getExistingContent().isBlank()) prompt.append("\n参考内容：\n").append(dto.getExistingContent()).append('\n');
        return prompt.append("\n请直接输出Markdown格式的写作结果，不要添加额外解释。").toString();
    }

    private void requireExistingContent(WritingRequestDTO dto) {
        if (dto.getExistingContent() == null || dto.getExistingContent().isBlank()) throw new IllegalArgumentException("该功能需要提供已有内容（existingContent）");
    }

    private String styleDescription(String style) { return "\n表达风格：" + valueOr(style, "formal") + "。"; }
    private String valueOr(String value, String fallback) { return value == null || value.isBlank() ? fallback : value; }
    private WritingTemplateVO template(String id, String name, String description, String category, String prompt, String type, String style) {
        return WritingTemplateVO.builder().id(id).name(name).description(description).category(category).prompt(prompt).suggestedContentType(type).suggestedStyle(style).build();
    }
}

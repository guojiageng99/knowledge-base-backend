package com.knowledge.base.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.knowledge.base.common.exception.BusinessException;
import com.knowledge.base.common.event.ReviewEventDTO;
import com.knowledge.base.common.result.PageResult;
import com.knowledge.base.common.utils.SnowflakeIdGenerator;
import com.knowledge.base.document.dto.DocumentReviewDTO;
import com.knowledge.base.document.dto.ReviewQueryDTO;
import com.knowledge.base.document.entity.Document;
import com.knowledge.base.document.entity.DocumentReview;
import com.knowledge.base.document.mapper.DocumentMapper;
import com.knowledge.base.document.mapper.DocumentReviewMapper;
import com.knowledge.base.document.service.DocumentReviewService;
import com.knowledge.base.document.vo.DocumentReviewVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentReviewServiceImpl extends ServiceImpl<DocumentReviewMapper, DocumentReview>
        implements DocumentReviewService {

    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_PUBLISHED = 1;
    private static final int STATUS_PENDING_REVIEW = 3;
    private static final int RESULT_APPROVED = 1;
    private static final int RESULT_REJECTED = 2;
    private static final long UNASSIGNED_REVIEWER_ID = 0L;
    private static final String REVIEW_EXCHANGE = "kb.notification.exchange";

    private final DocumentReviewMapper documentReviewMapper;
    private final DocumentMapper documentMapper;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submitForReview(Long documentId) {
        Document document = requireDocument(documentId);
        if (!Objects.equals(document.getStatus(), STATUS_DRAFT)
                && !Objects.equals(document.getStatus(), STATUS_PUBLISHED)) {
            throw new BusinessException("只有草稿或已发布状态的文档才能提交审核");
        }

        Integer latestRound = documentReviewMapper.countRoundsByDocumentId(documentId);
        DocumentReview review = new DocumentReview();
        review.setId(SnowflakeIdGenerator.nextId());
        review.setDocumentId(documentId);
        // The tutorial table makes reviewer_id non-null, so zero represents an unassigned reviewer.
        review.setReviewerId(UNASSIGNED_REVIEWER_ID);
        review.setReviewerName("待分配");
        review.setBeforeStatus(document.getStatus());
        review.setReviewRound((latestRound == null ? 0 : latestRound) + 1);
        review.setReviewLevel(1);
        review.setCreatedAt(LocalDateTime.now());
        if (documentReviewMapper.insert(review) <= 0) {
            throw new BusinessException("提交审核失败");
        }

        document.setStatus(STATUS_PENDING_REVIEW);
        if (documentMapper.updateById(document) <= 0) {
            throw new BusinessException("更新文档审核状态失败");
        }
        publishEvent(ReviewEventDTO.builder()
                .eventType("SUBMITTED").documentId(documentId).documentTitle(document.getTitle())
                .authorId(document.getAuthorId()).authorName(document.getAuthorName())
                .reviewRound(review.getReviewRound()).reviewLevel(review.getReviewLevel())
                .timestamp(LocalDateTime.now()).build(), "notification.review.submitted");
        log.info("Document submitted for review: documentId={}, round={}", documentId, review.getReviewRound());
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveReview(DocumentReviewDTO dto) {
        validateReviewResult(dto, RESULT_APPROVED);
        return finishReview(dto, RESULT_APPROVED, STATUS_PUBLISHED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean rejectReview(DocumentReviewDTO dto) {
        validateReviewResult(dto, RESULT_REJECTED);
        if (!StringUtils.hasText(dto.getReviewComment())) {
            throw new BusinessException("驳回意见不能为空");
        }
        return finishReview(dto, RESULT_REJECTED, STATUS_DRAFT);
    }

    @Override
    public PageResult<DocumentReviewVO> getPendingReviews(ReviewQueryDTO dto) {
        ReviewQueryDTO query = dto == null ? new ReviewQueryDTO() : dto;
        long current = query.getCurrent() == null || query.getCurrent() < 1 ? 1 : query.getCurrent();
        long size = query.getSize() == null || query.getSize() < 1 ? 10 : query.getSize();
        LambdaQueryWrapper<DocumentReview> wrapper = new LambdaQueryWrapper<DocumentReview>()
                .orderByDesc(DocumentReview::getCreatedAt);
        if (query.getStatus() == null || query.getStatus() == 0) {
            wrapper.isNull(DocumentReview::getReviewResult)
                    .exists("SELECT 1 FROM kb_document d WHERE d.id = tb_document_review.document_id "
                            + "AND d.status = " + STATUS_PENDING_REVIEW);
        } else if (query.getStatus() == RESULT_APPROVED || query.getStatus() == RESULT_REJECTED) {
            wrapper.eq(DocumentReview::getReviewResult, query.getStatus());
        } else {
            throw new BusinessException("审核状态只能为0、1或2");
        }
        if (query.getReviewerId() != null) {
            wrapper.eq(DocumentReview::getReviewerId, query.getReviewerId());
        }
        if (query.getAuthorId() != null) {
            wrapper.exists("SELECT 1 FROM kb_document d WHERE d.id = tb_document_review.document_id "
                    + "AND d.author_id = {0}", query.getAuthorId());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.exists("SELECT 1 FROM kb_document d WHERE d.id = tb_document_review.document_id "
                    + "AND d.title LIKE CONCAT('%', {0}, '%')", query.getKeyword());
        }
        IPage<DocumentReview> page = documentReviewMapper.selectPage(new Page<>(current, size), wrapper);
        List<DocumentReviewVO> records = page.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    @Override
    public List<DocumentReviewVO> getDocumentReviewHistory(Long documentId) {
        requireDocument(documentId);
        return documentReviewMapper.selectByDocumentId(documentId).stream().map(this::toVO).toList();
    }

    @Override
    public Long getPendingCount() {
        return documentReviewMapper.selectCount(new LambdaQueryWrapper<DocumentReview>()
                .isNull(DocumentReview::getReviewResult));
    }

    @Override
    public Map<String, Long> getReviewStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("pending", documentReviewMapper.selectCount(new LambdaQueryWrapper<DocumentReview>().isNull(DocumentReview::getReviewResult)));
        stats.put("approved", documentReviewMapper.selectCount(new LambdaQueryWrapper<DocumentReview>().eq(DocumentReview::getReviewResult, RESULT_APPROVED)));
        stats.put("rejected", documentReviewMapper.selectCount(new LambdaQueryWrapper<DocumentReview>().eq(DocumentReview::getReviewResult, RESULT_REJECTED)));
        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchReview(List<Long> taskIds, String status, String comment) {
        if (taskIds == null || taskIds.isEmpty()) throw new BusinessException("审核任务ID列表不能为空");
        for (Long taskId : taskIds) {
            DocumentReviewDTO dto = new DocumentReviewDTO();
            dto.setReviewId(taskId);
            dto.setReviewComment(comment);
            if ("approved".equalsIgnoreCase(status)) {
                dto.setReviewResult(RESULT_APPROVED);
                approveReview(dto);
            } else if ("rejected".equalsIgnoreCase(status)) {
                dto.setReviewResult(RESULT_REJECTED);
                rejectReview(dto);
            } else throw new BusinessException("无效的审核结果：" + status);
        }
    }

    private Boolean finishReview(DocumentReviewDTO dto, int result, int targetStatus) {
        DocumentReview review = requireReview(dto.getReviewId());
        if (review.getReviewResult() != null) {
            throw new BusinessException("该记录已审核");
        }
        Document document = requireDocument(review.getDocumentId());
        if (!Objects.equals(document.getStatus(), STATUS_PENDING_REVIEW)) {
            throw new BusinessException("文档当前不处于待审核状态");
        }

        review.setReviewerId(1L);
        review.setReviewerName("审核员");
        review.setReviewResult(result);
        review.setReviewComment(dto.getReviewComment());
        review.setReviewedAt(LocalDateTime.now());
        review.setReviewLevel(review.getReviewLevel() == null ? 1 : review.getReviewLevel());
        if (documentReviewMapper.updateById(review) <= 0) {
            throw new BusinessException("更新审核记录失败");
        }

        document.setStatus(targetStatus);
        if (targetStatus == STATUS_PUBLISHED) {
            document.setPublishTime(LocalDateTime.now());
        }
        if (documentMapper.updateById(document) <= 0) {
            throw new BusinessException("更新文档状态失败");
        }
        publishEvent(ReviewEventDTO.builder()
                .eventType(result == RESULT_APPROVED ? "APPROVED" : "REJECTED")
                .documentId(document.getId()).documentTitle(document.getTitle())
                .authorId(document.getAuthorId()).authorName(document.getAuthorName())
                .reviewerId(review.getReviewerId()).reviewerName(review.getReviewerName())
                .reviewRound(review.getReviewRound()).reviewLevel(review.getReviewLevel())
                .reviewComment(review.getReviewComment()).timestamp(LocalDateTime.now()).build(),
                result == RESULT_APPROVED ? "notification.review.approved" : "notification.review.rejected");
        log.info("Document review finished: reviewId={}, result={}", review.getId(), result);
        return true;
    }

    private void validateReviewResult(DocumentReviewDTO dto, int expectedResult) {
        if (dto == null || dto.getReviewId() == null) {
            throw new BusinessException("审核记录ID不能为空");
        }
        if (!Objects.equals(dto.getReviewResult(), expectedResult)) {
            throw new BusinessException(expectedResult == RESULT_APPROVED ? "审核通过的结果必须为1" : "审核驳回的结果必须为2");
        }
    }

    private Document requireDocument(Long documentId) {
        if (documentId == null) {
            throw new BusinessException("文档ID不能为空");
        }
        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException("文档不存在");
        }
        return document;
    }

    private DocumentReview requireReview(Long reviewId) {
        DocumentReview review = documentReviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException("审核记录不存在");
        }
        return review;
    }

    private DocumentReviewVO toVO(DocumentReview review) {
        Document document = documentMapper.selectById(review.getDocumentId());
        return DocumentReviewVO.builder()
                .id(review.getId())
                .documentId(review.getDocumentId())
                .documentTitle(document == null ? "" : document.getTitle())
                .reviewerId(review.getReviewerId())
                .reviewerName(review.getReviewerName())
                .reviewResult(review.getReviewResult())
                .reviewComment(review.getReviewComment())
                .beforeStatus(review.getBeforeStatus())
                .reviewedAt(review.getReviewedAt())
                .reviewRound(review.getReviewRound())
                .reviewLevel(review.getReviewLevel())
                .createdAt(review.getCreatedAt())
                .authorId(document == null ? null : document.getAuthorId())
                .authorName(document == null ? "" : document.getAuthorName())
                .categoryId(document == null ? null : document.getCategoryId())
                .build();
    }

    private void publishEvent(ReviewEventDTO event, String routingKey) {
        try {
            rabbitTemplate.convertAndSend(REVIEW_EXCHANGE, routingKey, event);
        } catch (Exception e) {
            log.warn("发布审核事件失败：documentId={}, eventType={}, error={}",
                    event.getDocumentId(), event.getEventType(), e.getMessage());
        }
    }
}

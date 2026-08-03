package com.knowledge.base.ai.service;
import com.knowledge.base.ai.dto.FeedbackRequestDTO;
public interface AiFeedbackService { boolean submit(FeedbackRequestDTO request, Long userId); }

package com.knowledge.base.file.consumer;

import com.knowledge.base.file.config.TranscodeRabbitConfig;
import com.knowledge.base.file.message.TranscodeMessage;
import com.knowledge.base.file.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "file.transcode.rabbit.enabled", havingValue = "true")
public class TranscodeConsumer {
    private final MediaService mediaService;
    @RabbitListener(queues = TranscodeRabbitConfig.QUEUE)
    public void consume(TranscodeMessage message) {
        try {
            mediaService.updateTranscodeStatus(message.getFileId(), "PROCESSING");
            if (mediaService.transcodeToHls(message.getFileId()) == null) throw new IllegalStateException("HLS transcoding failed");
            mediaService.generateThumbnail(message.getFileId());
            mediaService.updateTranscodeStatus(message.getFileId(), "DONE");
        } catch (Exception exception) {
            log.error("Media transcoding failed: {}", message, exception);
            mediaService.updateTranscodeStatus(message.getFileId(), "FAILED");
            throw exception;
        }
    }
}

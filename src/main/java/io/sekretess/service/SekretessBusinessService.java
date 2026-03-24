package io.sekretess.service;

import io.sekretess.dto.AdsMessageDTO;
import io.sekretess.dto.MessageDTO;
import io.sekretess.exception.MessageProcessingException;
import io.sekretess.exception.MessageSendException;
import io.sekretess.exception.PrekeyBundleException;
import io.sekretess.exception.SessionCreationException;
import io.sekretess.manager.SekretessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SekretessBusinessService {
    private final SekretessManager sekretessManager;
    private static final Logger logger = LoggerFactory.getLogger(SekretessBusinessService.class);


    public SekretessBusinessService(SekretessManager sekretessManager) {
        this.sekretessManager = sekretessManager;
    }

    public void handleSendMessage(MessageDTO messageDTO) {
        logger.info("Send message request received to send message to consumer: {}", messageDTO.getConsumer());
        try {
            this.sekretessManager.sendMessageToConsumer(messageDTO.getText(), messageDTO.getConsumer());
        } catch (SessionCreationException e) {
            logger.error("Session creation failed for consumer: {}", messageDTO.getConsumer(), e);
            throw new MessageProcessingException("Failed to create session for consumer: " + messageDTO.getConsumer(), e);
        } catch (PrekeyBundleException e) {
            logger.error("Prekey bundle retrieval failed for consumer: {}", messageDTO.getConsumer(), e);
            throw new MessageProcessingException("Failed to retrieve prekey bundle for consumer: " + messageDTO.getConsumer(), e);
        } catch (MessageSendException e) {
            logger.error("Message delivery failed for consumer: {}", messageDTO.getConsumer(), e);
            throw new MessageProcessingException("Failed to send message to consumer: " + messageDTO.getConsumer(), e);
        }
    }


    public void handleSendAdsMessage(AdsMessageDTO adsMessageDTO) {
        logger.info("Send ads message request received to send ads message to consumers!");
        try {
            this.sekretessManager.sendAdsMessage(adsMessageDTO.getText());
        } catch (MessageSendException e) {
            logger.error("Ads message delivery failed", e);
            throw new MessageProcessingException("Failed to send ads message", e);
        }
    }

}

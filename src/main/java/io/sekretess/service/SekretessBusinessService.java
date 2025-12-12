package io.sekretess.service;

import io.sekretess.dto.AdsMessageDTO;
import io.sekretess.dto.MessageDTO;
import io.sekretess.manager.SekretessManager;
import io.sekretess.store.SekretessSignalProtocolStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SekretessBusinessService {
    private final SekretessSignalProtocolStore sekretessInMemorySignalProtocolStore;
    private final SekretessManager sekretessManager;
    private static final Logger logger = LoggerFactory.getLogger(SekretessBusinessService.class);


    public SekretessBusinessService(SekretessSignalProtocolStore sekretessInMemorySignalProtocolStore,
                                    SekretessManager sekretessManager) {
        this.sekretessInMemorySignalProtocolStore = sekretessInMemorySignalProtocolStore;
        this.sekretessManager = sekretessManager;

    }

    public void deleteSession(String user) {
        logger.info("Delete user session request received! {}", user);
        sekretessInMemorySignalProtocolStore.deleteSession(new SignalProtocolAddress(user, 123));
    }

    public void handleSendMessage(MessageDTO messageDTO) {
        logger.info("Send message request received to send message to consumer: {}", messageDTO.getConsumer());
        this.sekretessManager.sendMessageToConsumer(messageDTO.getText(), messageDTO.getConsumer());
    }


    public void handleSendAdsMessage(AdsMessageDTO adsMessageDTO) {
        logger.info("Send ads message request received to send ads message to consumers!");
        this.sekretessManager.sendAdsMessage(adsMessageDTO.getText());
    }

}

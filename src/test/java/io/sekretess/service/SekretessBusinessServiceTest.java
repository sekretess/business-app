package io.sekretess.service;

import io.sekretess.dto.AdsMessageDTO;
import io.sekretess.dto.MessageDTO;
import io.sekretess.exception.MessageProcessingException;
import io.sekretess.exception.MessageSendException;
import io.sekretess.exception.PrekeyBundleException;
import io.sekretess.exception.SessionCreationException;
import io.sekretess.manager.SekretessManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SekretessBusinessServiceTest {

    @Mock
    private SekretessManager sekretessManager;

    private SekretessBusinessService service;

    @BeforeEach
    void setUp() {
        service = new SekretessBusinessService(sekretessManager);
    }

    @Test
    void handleSendMessage_shouldDelegateToManager() throws Exception {
        MessageDTO dto = new MessageDTO();
        dto.setText("Hello");
        dto.setConsumer("user123");

        service.handleSendMessage(dto);

        verify(sekretessManager).sendMessageToConsumer("Hello", "user123");
    }

    @Test
    void handleSendMessage_shouldThrowMessageProcessingException_whenSessionCreationFails() throws Exception {
        MessageDTO dto = new MessageDTO();
        dto.setText("Hello");
        dto.setConsumer("user123");

        doThrow(new SessionCreationException("Session failed"))
                .when(sekretessManager).sendMessageToConsumer(anyString(), anyString());

        MessageProcessingException ex = assertThrows(MessageProcessingException.class,
                () -> service.handleSendMessage(dto));

        assertTrue(ex.getMessage().contains("Failed to create session"));
        assertTrue(ex.getMessage().contains("user123"));
        assertInstanceOf(SessionCreationException.class, ex.getCause());
    }

    @Test
    void handleSendMessage_shouldThrowMessageProcessingException_whenPrekeyBundleFails() throws Exception {
        MessageDTO dto = new MessageDTO();
        dto.setText("Hello");
        dto.setConsumer("user123");

        doThrow(new PrekeyBundleException("Prekey failed"))
                .when(sekretessManager).sendMessageToConsumer(anyString(), anyString());

        MessageProcessingException ex = assertThrows(MessageProcessingException.class,
                () -> service.handleSendMessage(dto));

        assertTrue(ex.getMessage().contains("Failed to retrieve prekey bundle"));
        assertInstanceOf(PrekeyBundleException.class, ex.getCause());
    }

    @Test
    void handleSendMessage_shouldThrowMessageProcessingException_whenMessageSendFails() throws Exception {
        MessageDTO dto = new MessageDTO();
        dto.setText("Hello");
        dto.setConsumer("user123");

        doThrow(new MessageSendException("Send failed"))
                .when(sekretessManager).sendMessageToConsumer(anyString(), anyString());

        MessageProcessingException ex = assertThrows(MessageProcessingException.class,
                () -> service.handleSendMessage(dto));

        assertTrue(ex.getMessage().contains("Failed to send message"));
        assertInstanceOf(MessageSendException.class, ex.getCause());
    }

    @Test
    void handleSendAdsMessage_shouldDelegateToManager() throws Exception {
        AdsMessageDTO dto = new AdsMessageDTO();
        dto.setText("Special offer!");

        service.handleSendAdsMessage(dto);

        verify(sekretessManager).sendAdsMessage("Special offer!");
    }

    @Test
    void handleSendAdsMessage_shouldThrowMessageProcessingException_whenMessageSendFails() throws Exception {
        AdsMessageDTO dto = new AdsMessageDTO();
        dto.setText("Special offer!");

        doThrow(new MessageSendException("Ads send failed"))
                .when(sekretessManager).sendAdsMessage(anyString());

        MessageProcessingException ex = assertThrows(MessageProcessingException.class,
                () -> service.handleSendAdsMessage(dto));

        assertTrue(ex.getMessage().contains("Failed to send ads message"));
        assertInstanceOf(MessageSendException.class, ex.getCause());
    }
}

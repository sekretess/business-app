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
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    void handleSendFile_shouldDelegateToManagerAndDeleteTempFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "hello.txt",
                "text/plain",
                "Hello file".getBytes()
        );
        AtomicReference<Path> uploadedPath = new AtomicReference<>();

        doAnswer(invocation -> {
            Path path = invocation.getArgument(0);
            uploadedPath.set(path);
            assertTrue(Files.exists(path));
            assertEquals("Hello file", Files.readString(path));
            return null;
        }).when(sekretessManager).sendFileToConsumer(any(Path.class), eq("user123"));

        service.handleSendFile(file, "user123");

        verify(sekretessManager).sendFileToConsumer(any(Path.class), eq("user123"));
        assertNotNull(uploadedPath.get());
        assertFalse(Files.exists(uploadedPath.get()));
    }

    @Test
    void handleSendFile_shouldThrowMessageProcessingException_whenMessageSendFails() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "hello.txt",
                "text/plain",
                "Hello file".getBytes()
        );
        AtomicReference<Path> uploadedPath = new AtomicReference<>();

        doAnswer(invocation -> {
            uploadedPath.set(invocation.getArgument(0));
            throw new MessageSendException("Send failed");
        }).when(sekretessManager).sendFileToConsumer(any(Path.class), eq("user123"));

        MessageProcessingException ex = assertThrows(MessageProcessingException.class,
                () -> service.handleSendFile(file, "user123"));

        assertTrue(ex.getMessage().contains("Failed to send file"));
        assertInstanceOf(MessageSendException.class, ex.getCause());
        assertNotNull(uploadedPath.get());
        assertFalse(Files.exists(uploadedPath.get()));
    }

    @Test
    void handleSendFile_shouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.handleSendFile(file, "user123"));

        assertEquals("File cannot be empty", ex.getMessage());
        verifyNoInteractions(sekretessManager);
    }

    @Test
    void handleSendFile_shouldRejectBlankConsumer() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "hello.txt",
                "text/plain",
                "Hello file".getBytes()
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.handleSendFile(file, " "));

        assertEquals("Consumer cannot be blank", ex.getMessage());
        verifyNoInteractions(sekretessManager);
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

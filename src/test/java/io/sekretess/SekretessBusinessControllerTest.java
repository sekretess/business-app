package io.sekretess;

import io.sekretess.controller.SekretessBusinessController;
import io.sekretess.exception.GlobalExceptionHandler;
import io.sekretess.exception.MessageSendException;
import io.sekretess.exception.PrekeyBundleException;
import io.sekretess.exception.SessionCreationException;
import io.sekretess.manager.SekretessManager;
import io.sekretess.service.SekretessBusinessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SekretessBusinessController.class)
@Import({SekretessBusinessService.class, GlobalExceptionHandler.class})
public class SekretessBusinessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SekretessManager sekretessManager;

    @Test
    void sendMessage_shouldReturnAccepted_whenMessageSentSuccessfully() throws Exception {
        // Given
        doNothing().when(sekretessManager).sendMessageToConsumer(anyString(), anyString());

        // When & Then
        mockMvc.perform(post("/api/v1/business/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\": \"Hello World\", \"consumer\": \"user123\"}"))
                .andExpect(status().isAccepted());

        // Verify
        verify(sekretessManager).sendMessageToConsumer("Hello World", "user123");
    }

    @Test
    void sendMessage_shouldReturnInternalServerError_whenSessionCreationFails() throws Exception {
        // Given
        doThrow(new SessionCreationException("Session failed"))
                .when(sekretessManager).sendMessageToConsumer(anyString(), anyString());

        // When & Then
        mockMvc.perform(post("/api/v1/business/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\": \"Hello\", \"consumer\": \"user123\"}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void sendMessage_shouldReturnInternalServerError_whenMessageSendFails() throws Exception {
        // Given
        doThrow(new MessageSendException("Send failed"))
                .when(sekretessManager).sendMessageToConsumer(anyString(), anyString());

        // When & Then
        mockMvc.perform(post("/api/v1/business/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\": \"Hello\", \"consumer\": \"user123\"}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void sendMessage_shouldReturnInternalServerError_whenPrekeyBundleFails() throws Exception {
        // Given
        doThrow(new PrekeyBundleException("Prekey failed"))
                .when(sekretessManager).sendMessageToConsumer(anyString(), anyString());

        // When & Then
        mockMvc.perform(post("/api/v1/business/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\": \"Hello\", \"consumer\": \"user123\"}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void sendAdsMessage_shouldReturnAccepted_whenAdsMessageSentSuccessfully() throws Exception {
        // Given
        doNothing().when(sekretessManager).sendAdsMessage(anyString());

        // When & Then
        mockMvc.perform(post("/api/v1/business/ads/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\": \"Special offer!\"}"))
                .andExpect(status().isAccepted());

        // Verify
        verify(sekretessManager).sendAdsMessage("Special offer!");
    }

    @Test
    void sendAdsMessage_shouldReturnInternalServerError_whenMessageSendFails() throws Exception {
        // Given
        doThrow(new MessageSendException("Ads send failed"))
                .when(sekretessManager).sendAdsMessage(anyString());

        // When & Then
        mockMvc.perform(post("/api/v1/business/ads/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\": \"Special offer!\"}"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void sendMessage_shouldReturnBadRequest_whenRequestBodyIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/business/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json"))
                .andExpect(status().isBadRequest());
    }
}

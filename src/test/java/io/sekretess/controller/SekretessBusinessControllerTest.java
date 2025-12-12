//package io.sekretess.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.sekretess.dto.AdsMessageDTO;
//import io.sekretess.dto.MessageDTO;
//import io.sekretess.service.SekretessBusinessService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.times;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(SekretessBusinessController.class)
//class SekretessBusinessControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    private SekretessBusinessService service;
//
//    @Test
//    void postMessages_returnsAccepted_and_callsService() throws Exception {
//        MessageDTO dto = new MessageDTO();
//        dto.setText("hi");
//        dto.setConsumer("bob");
//
//        mockMvc.perform(post("/api/v1/business/messages")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(dto)))
//                .andExpect(status().isAccepted());
//
//        verify(service, times(1)).handleSendMessage(dto);
//    }
//
//    @Test
//    void postAdsMessages_returnsAccepted_and_callsService() throws Exception {
//        AdsMessageDTO dto = new AdsMessageDTO();
//        dto.setText("promo");
//
//        mockMvc.perform(post("/api/v1/business/ads/messages")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(dto)))
//                .andExpect(status().isAccepted());
//
//        verify(service, times(1)).handleSendAdsMessage(dto);
//    }
//
//    @Test
//    void deleteSession_returnsNoContent_and_callsService() throws Exception {
//        String user = "carol";
//        mockMvc.perform(delete("/api/v1/business/sessions/users/{userName}", user))
//                .andExpect(status().isNoContent());
//
//        verify(service, times(1)).deleteSession(user);
//    }
//}
//

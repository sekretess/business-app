//package io.sekretess.service;
//
//import io.sekretess.dto.AdsMessageDTO;
//import io.sekretess.dto.MessageDTO;
//import io.sekretess.manager.SekretessManager;
//import io.sekretess.store.SekretessSignalProtocolStore;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.signal.libsignal.protocol.SignalProtocolAddress;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class SekretessBusinessServiceTest {
//
//    @Mock
//    private SekretessSignalProtocolStore store;
//
//    @Mock
//    private SekretessManager manager;
//
//    @InjectMocks
//    private SekretessBusinessService service;
//
//    @Test
//    void deleteSession_delegatesToStore_withCorrectAddress() {
//        String user = "alice";
//        service.deleteSession(user);
//
//        ArgumentCaptor<SignalProtocolAddress> captor = ArgumentCaptor.forClass(SignalProtocolAddress.class);
//        verify(store, times(1)).deleteSession(captor.capture());
//        SignalProtocolAddress addr = captor.getValue();
//        assertEquals(user, addr.getName());
//        assertEquals(123, addr.getDeviceId());
//    }
//
//    @Test
//    void handleSendMessage_callsManagerWithTextAndConsumer() {
//        MessageDTO dto = new MessageDTO();
//        dto.setText("hello");
//        dto.setConsumer("bob");
//
//        service.handleSendMessage(dto);
//
//        verify(manager, times(1)).sendMessageToConsumer("hello", "bob");
//    }
//
//    @Test
//    void handleSendAdsMessage_callsManagerWithText() {
//        AdsMessageDTO dto = new AdsMessageDTO();
//        dto.setText("sale");
//
//        service.handleSendAdsMessage(dto);
//
//        verify(manager, times(1)).sendAdsMessage("sale");
//    }
//
//    @Test
//    void deleteSession_nullUser_throwsNullPointerException() {
//        assertThrows(NullPointerException.class, () -> service.deleteSession(null));
//    }
//}
//

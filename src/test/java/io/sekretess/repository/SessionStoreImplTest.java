//package io.sekretess.repository;
//
//import io.sekretess.model.SessionModel;
//import io.sekretess.model.SessionData;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.List;
//import java.util.ArrayList;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class SessionStoreImplTest {
//
//    @Mock
//    private SessionRepository sessionRepository;
//
//    @InjectMocks
//    private SessionStoreImpl store;
//
//    @Test
//    void saveSession_savesModelWithFields() {
//        store.saveSession("dave", 5, "record123");
//
//        ArgumentCaptor<SessionModel> captor = ArgumentCaptor.forClass(SessionModel.class);
//        verify(sessionRepository, times(1)).save(captor.capture());
//        SessionModel model = captor.getValue();
//        assertEquals("dave", model.getName());
//        assertEquals(5, model.getDeviceId());
//        assertEquals("record123", model.getSessionRecord());
//    }
//
//    @Test
//    void loadAll_returnsMappedSessionDataList() {
//        List<SessionModel> models = new ArrayList<>();
//        SessionModel m1 = new SessionModel();
//        m1.setName("x");
//        m1.setDeviceId(1);
//        m1.setSessionRecord("r1");
//        models.add(m1);
//
//        SessionModel m2 = new SessionModel();
//        m2.setName("y");
//        m2.setDeviceId(2);
//        m2.setSessionRecord("r2");
//        models.add(m2);
//
//        when(sessionRepository.findAll()).thenReturn(models);
//
//        List<SessionData> result = store.loadAll();
//        assertEquals(2, result.size());
//        assertEquals("x", result.get(0).getName());
//        assertEquals(1, result.get(0).getDeviceId());
//        assertEquals("r1", result.get(0).getSessionRecord());
//    }
//
//    @Test
//    void deleteSession_delegatesToRepository() {
//        store.deleteSession("z");
//        verify(sessionRepository, times(1)).deleteById("z");
//    }
//}
//

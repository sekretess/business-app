//package io.sekretess.repository;
//
//import io.sekretess.model.GroupSessionModel;
//import io.sekretess.model.GroupSessionData;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class GroupSessionStoreImplTest {
//
//    @Mock
//    private GroupSessionRepository groupSessionRepository;
//
//    @InjectMocks
//    private GroupSessionStoreImpl store;
//
//    @Test
//    void saveGroupSession_whenNotPresent_createsAndSaves() {
//        String name = "grp1";
//        when(groupSessionRepository.findById(name)).thenReturn(Optional.empty());
//
//        store.saveGroupSession(name, 7, "dist1", "msg", "sessionRec");
//
//        ArgumentCaptor<GroupSessionModel> captor = ArgumentCaptor.forClass(GroupSessionModel.class);
//        verify(groupSessionRepository, times(1)).save(captor.capture());
//        GroupSessionModel saved = captor.getValue();
//        assertEquals(name, saved.getName());
//        assertEquals(7, saved.getDeviceId());
//        assertEquals("msg", saved.getDistributionMessage());
//        assertEquals("dist1", saved.getDistributionId());
//    }
//
//    @Test
//    void saveGroupSession_whenPresent_updatesAndSaves() {
//        String name = "grp2";
//        GroupSessionModel existing = new GroupSessionModel();
//        existing.setName(name);
//        existing.setDeviceId(1);
//        existing.setDistributionMessage("old");
//        existing.setDistributionId("oldId");
//        when(groupSessionRepository.findById(name)).thenReturn(Optional.of(existing));
//
//        store.saveGroupSession(name, 8, "newId", "newMsg", "sessionRec");
//
//        ArgumentCaptor<GroupSessionModel> captor = ArgumentCaptor.forClass(GroupSessionModel.class);
//        verify(groupSessionRepository, times(1)).save(captor.capture());
//        GroupSessionModel saved = captor.getValue();
//        assertEquals(name, saved.getName());
//        assertEquals(8, saved.getDeviceId());
//        assertEquals("newMsg", saved.getDistributionMessage());
//        assertEquals("newId", saved.getDistributionId());
//    }
//
//    @Test
//    void loadGroupSession_whenPresent_returnsData() {
//        String name = "g1";
//        GroupSessionModel model = new GroupSessionModel();
//        model.setName(name);
//        model.setDeviceId(3);
//        model.setDistributionId("did");
//        model.setSessionRecord("rec");
//        when(groupSessionRepository.findById(name)).thenReturn(Optional.of(model));
//
//        GroupSessionData data = store.loadGroupSession(name);
//        assertNotNull(data);
//        assertEquals(name, data.getName());
//        assertEquals(3, data.getDeviceId());
//        assertEquals("did", data.getDistributionId());
//        assertEquals("rec", data.getSessionRecord());
//    }
//
//    @Test
//    void loadGroupSession_whenAbsent_returnsNull() {
//        when(groupSessionRepository.findById("nope")).thenReturn(Optional.empty());
//        assertNull(store.loadGroupSession("nope"));
//    }
//}
//

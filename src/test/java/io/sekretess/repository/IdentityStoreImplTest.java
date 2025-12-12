//package io.sekretess.repository;
//
//import io.sekretess.model.IdentityKeyModel;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Base64;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class IdentityStoreImplTest {
//
//    @Mock
//    private IdentityKeyRepository identityKeyRepository;
//
//    @InjectMocks
//    private IdentityStoreImpl store;
//
//    @Test
//    void loadIdentity_whenPresent_returnsDecodedIdentityKeyData() {
//        String username = "alice";
//        byte[] keyBytes = new byte[]{1,2,3,4};
//        IdentityKeyModel model = new IdentityKeyModel();
//        model.setUserName(username);
//        model.setIdentityKey(Base64.getEncoder().encodeToString(keyBytes));
//        model.setRegistrationId(99);
//
//        when(identityKeyRepository.findById(username)).thenReturn(Optional.of(model));
//
//        IdentityKeyData data = store.loadIdentity(username);
//        assertNotNull(data);
//        assertArrayEquals(keyBytes, data.getIdentityKey());
//        assertEquals(99, data.getRegistrationId());
//    }
//
//    @Test
//    void loadIdentity_whenAbsent_returnsNull() {
//        when(identityKeyRepository.findById("bob")).thenReturn(Optional.empty());
//        IdentityKeyData data = store.loadIdentity("bob");
//        assertNull(data);
//    }
//
//    @Test
//    void saveIdentity_encodesBytesAndSavesModel() {
//        String username = "carol";
//        byte[] bytes = new byte[]{5,6,7};
//        int regId = 42;
//
//        store.saveIdentity(username, bytes, regId);
//
//        ArgumentCaptor<IdentityKeyModel> captor = ArgumentCaptor.forClass(IdentityKeyModel.class);
//        verify(identityKeyRepository, times(1)).save(captor.capture());
//        IdentityKeyModel saved = captor.getValue();
//        assertEquals(username, saved.getUserName());
//        assertEquals(Base64.getEncoder().encodeToString(bytes), saved.getIdentityKey());
//        assertEquals(regId, saved.getRegistrationId());
//    }
//}
//

package io.sekretess.repository;

import io.sekretess.model.IdentityKeyData;
import io.sekretess.model.IdentityKeyModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentityStoreImplTest {

    @Mock
    private IdentityKeyRepository identityKeyRepository;

    private IdentityStoreImpl identityStore;

    @BeforeEach
    void setUp() {
        identityStore = new IdentityStoreImpl(identityKeyRepository);
    }

    @Test
    void loadIdentity_shouldReturnIdentityKeyData_whenUserExists() {
        // Given
        String username = "testUser";
        byte[] identityKeyBytes = "testIdentityKey".getBytes();
        String base64IdentityKey = Base64.getEncoder().encodeToString(identityKeyBytes);
        int registrationId = 12345;

        IdentityKeyModel model = new IdentityKeyModel();
        model.setUserName(username);
        model.setIdentityKey(base64IdentityKey);
        model.setRegistrationId(registrationId);

        when(identityKeyRepository.findById(username)).thenReturn(Optional.of(model));

        // When
        IdentityKeyData result = identityStore.loadIdentity(username);

        // Then
        assertNotNull(result);
        assertEquals(username, result.username());
        assertArrayEquals(identityKeyBytes, result.serializedIdentityKeyPair());
        assertEquals(registrationId, result.registrationId());
        verify(identityKeyRepository).findById(username);
    }

    @Test
    void loadIdentity_shouldReturnNull_whenUserDoesNotExist() {
        // Given
        String username = "nonExistentUser";
        when(identityKeyRepository.findById(username)).thenReturn(Optional.empty());

        // When
        IdentityKeyData result = identityStore.loadIdentity(username);

        // Then
        assertNull(result);
        verify(identityKeyRepository).findById(username);
    }

    @Test
    void saveIdentity_shouldSaveIdentityKeyModel() {
        // Given
        String userName = "newUser";
        byte[] identityKeyBytes = "newIdentityKey".getBytes();
        int registrationId = 54321;

        // When
        identityStore.saveIdentity(userName, identityKeyBytes, registrationId);

        // Then
        ArgumentCaptor<IdentityKeyModel> captor = ArgumentCaptor.forClass(IdentityKeyModel.class);
        verify(identityKeyRepository).save(captor.capture());

        IdentityKeyModel savedModel = captor.getValue();
        assertEquals(userName, savedModel.getUserName());
        assertEquals(Base64.getEncoder().encodeToString(identityKeyBytes), savedModel.getIdentityKey());
        assertEquals(registrationId, savedModel.getRegistrationId());
    }

    @Test
    void saveIdentity_shouldEncodeIdentityKeyAsBase64() {
        // Given
        String userName = "user";
        byte[] identityKeyBytes = {0x01, 0x02, 0x03, 0x04, 0x05};
        int registrationId = 100;

        // When
        identityStore.saveIdentity(userName, identityKeyBytes, registrationId);

        // Then
        ArgumentCaptor<IdentityKeyModel> captor = ArgumentCaptor.forClass(IdentityKeyModel.class);
        verify(identityKeyRepository).save(captor.capture());

        String expectedBase64 = Base64.getEncoder().encodeToString(identityKeyBytes);
        assertEquals(expectedBase64, captor.getValue().getIdentityKey());
    }
}

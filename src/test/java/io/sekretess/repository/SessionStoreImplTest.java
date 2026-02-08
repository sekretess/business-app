package io.sekretess.repository;

import io.sekretess.model.SessionData;
import io.sekretess.model.SessionModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionStoreImplTest {

    @Mock
    private SessionRepository sessionRepository;

    private SessionStoreImpl sessionStore;

    @BeforeEach
    void setUp() {
        sessionStore = new SessionStoreImpl(sessionRepository);
    }

    @Test
    void saveSession_shouldSaveSessionModel() {
        // Given
        String consumerName = "consumer1";
        int deviceId = 1;
        String base64EncodedRecord = "encodedSessionRecord";

        // When
        sessionStore.saveSession(consumerName, deviceId, base64EncodedRecord);

        // Then
        ArgumentCaptor<SessionModel> captor = ArgumentCaptor.forClass(SessionModel.class);
        verify(sessionRepository).save(captor.capture());

        SessionModel savedModel = captor.getValue();
        assertEquals(consumerName, savedModel.getName());
        assertEquals(deviceId, savedModel.getDeviceId());
        assertEquals(base64EncodedRecord, savedModel.getSessionRecord());
    }

    @Test
    void loadAll_shouldReturnListOfSessionData_whenSessionsExist() {
        // Given
        SessionModel model1 = new SessionModel();
        model1.setName("consumer1");
        model1.setDeviceId(1);
        model1.setSessionRecord("record1");

        SessionModel model2 = new SessionModel();
        model2.setName("consumer2");
        model2.setDeviceId(2);
        model2.setSessionRecord("record2");

        when(sessionRepository.findAll()).thenReturn(Arrays.asList(model1, model2));

        // When
        List<SessionData> result = sessionStore.loadAll();

        // Then
        assertEquals(2, result.size());

        assertEquals("consumer1", result.get(0).name());
        assertEquals(1, result.get(0).deviceId());
        assertEquals("record1", result.get(0).base64SessionRecord());

        assertEquals("consumer2", result.get(1).name());
        assertEquals(2, result.get(1).deviceId());
        assertEquals("record2", result.get(1).base64SessionRecord());

        verify(sessionRepository).findAll();
    }

    @Test
    void loadAll_shouldReturnEmptyList_whenNoSessionsExist() {
        // Given
        when(sessionRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<SessionData> result = sessionStore.loadAll();

        // Then
        assertTrue(result.isEmpty());
        verify(sessionRepository).findAll();
    }

    @Test
    void deleteSession_shouldDeleteByName() {
        // Given
        String name = "sessionToDelete";

        // When
        sessionStore.deleteSession(name);

        // Then
        verify(sessionRepository).deleteById(name);
    }

    @Test
    void saveSession_shouldOverwriteExistingSession_whenSameNameUsed() {
        // Given
        String consumerName = "existingConsumer";
        int newDeviceId = 99;
        String newRecord = "newEncodedRecord";

        // When
        sessionStore.saveSession(consumerName, newDeviceId, newRecord);

        // Then
        ArgumentCaptor<SessionModel> captor = ArgumentCaptor.forClass(SessionModel.class);
        verify(sessionRepository).save(captor.capture());

        SessionModel savedModel = captor.getValue();
        assertEquals(consumerName, savedModel.getName());
        assertEquals(newDeviceId, savedModel.getDeviceId());
        assertEquals(newRecord, savedModel.getSessionRecord());
    }
}

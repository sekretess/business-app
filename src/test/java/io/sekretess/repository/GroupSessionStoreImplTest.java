package io.sekretess.repository;

import io.sekretess.model.GroupSessionData;
import io.sekretess.model.GroupSessionModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupSessionStoreImplTest {

    @Mock
    private GroupSessionRepository groupSessionRepository;

    private GroupSessionStoreImpl groupSessionStore;

    @BeforeEach
    void setUp() {
        groupSessionStore = new GroupSessionStoreImpl(groupSessionRepository);
    }

    @Test
    void saveGroupSession_shouldCreateNewSession_whenNotExists() {
        // Given
        String name = "groupSession1";
        int deviceId = 1;
        String distributionId = "distId123";
        String sessionRecord = "sessionRecordData";

        when(groupSessionRepository.findById(name)).thenReturn(Optional.empty());

        // When
        groupSessionStore.saveGroupSession(name, deviceId, distributionId, sessionRecord);

        // Then
        ArgumentCaptor<GroupSessionModel> captor = ArgumentCaptor.forClass(GroupSessionModel.class);
        verify(groupSessionRepository).save(captor.capture());

        GroupSessionModel savedModel = captor.getValue();
        assertEquals(name, savedModel.getName());
        assertEquals(deviceId, savedModel.getDeviceId());
        assertEquals(distributionId, savedModel.getDistributionId());
        assertEquals(sessionRecord, savedModel.getSessionRecord());
    }

    @Test
    void saveGroupSession_shouldUpdateExistingSession_whenExists() {
        // Given
        String name = "existingGroupSession";
        int newDeviceId = 2;
        String newDistributionId = "newDistId";
        String newSessionRecord = "newSessionRecord";

        GroupSessionModel existingModel = new GroupSessionModel();
        existingModel.setName(name);
        existingModel.setDeviceId(1);
        existingModel.setDistributionId("oldDistId");
        existingModel.setSessionRecord("oldRecord");

        when(groupSessionRepository.findById(name)).thenReturn(Optional.of(existingModel));

        // When
        groupSessionStore.saveGroupSession(name, newDeviceId, newDistributionId, newSessionRecord);

        // Then
        ArgumentCaptor<GroupSessionModel> captor = ArgumentCaptor.forClass(GroupSessionModel.class);
        verify(groupSessionRepository).save(captor.capture());

        GroupSessionModel savedModel = captor.getValue();
        assertEquals(name, savedModel.getName());
        assertEquals(newDeviceId, savedModel.getDeviceId());
        assertEquals(newDistributionId, savedModel.getDistributionId());
        assertEquals(newSessionRecord, savedModel.getSessionRecord());
    }

    @Test
    void saveSendDistributionMessage_shouldCreateNewSession_whenNotExists() {
        // Given
        String name = "newSession";
        int deviceId = 3;
        String distributionId = "distId";
        String distributionMessage = "distributionMessageData";

        when(groupSessionRepository.findById(name)).thenReturn(Optional.empty());

        // When
        groupSessionStore.saveSendDistributionMessage(name, deviceId, distributionId, distributionMessage);

        // Then
        ArgumentCaptor<GroupSessionModel> captor = ArgumentCaptor.forClass(GroupSessionModel.class);
        verify(groupSessionRepository).save(captor.capture());

        GroupSessionModel savedModel = captor.getValue();
        assertEquals(name, savedModel.getName());
        assertEquals(deviceId, savedModel.getDeviceId());
        assertEquals(distributionId, savedModel.getDistributionId());
        assertEquals(distributionMessage, savedModel.getDistributionMessage());
    }

    @Test
    void saveSendDistributionMessage_shouldUpdateExistingSession_whenExists() {
        // Given
        String name = "existingSession";
        int newDeviceId = 4;
        String newDistributionId = "newDistId";
        String newDistributionMessage = "newDistMessage";

        GroupSessionModel existingModel = new GroupSessionModel();
        existingModel.setName(name);
        existingModel.setDeviceId(1);
        existingModel.setDistributionId("oldDistId");
        existingModel.setDistributionMessage("oldMessage");

        when(groupSessionRepository.findById(name)).thenReturn(Optional.of(existingModel));

        // When
        groupSessionStore.saveSendDistributionMessage(name, newDeviceId, newDistributionId, newDistributionMessage);

        // Then
        ArgumentCaptor<GroupSessionModel> captor = ArgumentCaptor.forClass(GroupSessionModel.class);
        verify(groupSessionRepository).save(captor.capture());

        GroupSessionModel savedModel = captor.getValue();
        assertEquals(name, savedModel.getName());
        assertEquals(newDeviceId, savedModel.getDeviceId());
        assertEquals(newDistributionId, savedModel.getDistributionId());
        assertEquals(newDistributionMessage, savedModel.getDistributionMessage());
    }

    @Test
    void loadGroupSession_shouldReturnGroupSessionData_whenExists() {
        // Given
        String name = "existingGroup";
        int deviceId = 5;
        String distributionId = "distId456";
        String sessionRecord = "recordData";
        String distributionMessage = "messageData";

        GroupSessionModel model = new GroupSessionModel();
        model.setName(name);
        model.setDeviceId(deviceId);
        model.setDistributionId(distributionId);
        model.setSessionRecord(sessionRecord);
        model.setDistributionMessage(distributionMessage);

        when(groupSessionRepository.findById(name)).thenReturn(Optional.of(model));

        // When
        GroupSessionData result = groupSessionStore.loadGroupSession(name);

        // Then
        assertNotNull(result);
        assertEquals(name, result.name());
        assertEquals(deviceId, result.deviceId());
        assertEquals(distributionId, result.distributionId());
        assertEquals(sessionRecord, result.sessionRecord());
        assertEquals(distributionMessage, result.businessDistributionMessage());
        verify(groupSessionRepository).findById(name);
    }

    @Test
    void loadGroupSession_shouldReturnNull_whenNotExists() {
        // Given
        String name = "nonExistentGroup";
        when(groupSessionRepository.findById(name)).thenReturn(Optional.empty());

        // When
        GroupSessionData result = groupSessionStore.loadGroupSession(name);

        // Then
        assertNull(result);
        verify(groupSessionRepository).findById(name);
    }

    @Test
    void loadGroupSession_shouldHandleNullDistributionMessage() {
        // Given
        String name = "groupWithNullMessage";
        GroupSessionModel model = new GroupSessionModel();
        model.setName(name);
        model.setDeviceId(1);
        model.setDistributionId("distId");
        model.setSessionRecord("record");
        model.setDistributionMessage(null);

        when(groupSessionRepository.findById(name)).thenReturn(Optional.of(model));

        // When
        GroupSessionData result = groupSessionStore.loadGroupSession(name);

        // Then
        assertNotNull(result);
        assertNull(result.businessDistributionMessage());
    }
}

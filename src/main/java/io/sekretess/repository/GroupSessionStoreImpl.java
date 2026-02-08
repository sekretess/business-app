package io.sekretess.repository;

import io.sekretess.model.GroupSessionData;
import io.sekretess.model.GroupSessionModel;
import io.sekretess.store.GroupSessionStore;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GroupSessionStoreImpl implements GroupSessionStore {
    private final GroupSessionRepository groupSessionRepository;

    public GroupSessionStoreImpl(GroupSessionRepository groupSessionRepository) {
        this.groupSessionRepository = groupSessionRepository;
    }

    @Override
    public void saveGroupSession(String name, int deviceId, String distributionId, String sessionRecord) {
        Optional<GroupSessionModel> optionalGroupSessionModel = groupSessionRepository.findById(name);
        GroupSessionModel groupSessionModel;
        groupSessionModel = optionalGroupSessionModel.orElseGet(GroupSessionModel::new);
        groupSessionModel.setName(name);
        groupSessionModel.setDeviceId(deviceId);
        groupSessionModel.setDistributionId(distributionId);
        groupSessionModel.setSessionRecord(sessionRecord);
        groupSessionRepository.save(groupSessionModel);
    }


    @Override
    public void saveSendDistributionMessage(String name, int deviceId, String distributionId, String businessDistributionMessage) {
        Optional<GroupSessionModel> optionalGroupSessionModel = groupSessionRepository.findById(name);
        GroupSessionModel groupSessionModel;
        groupSessionModel = optionalGroupSessionModel.orElseGet(GroupSessionModel::new);
        groupSessionModel.setName(name);
        groupSessionModel.setDeviceId(deviceId);
        groupSessionModel.setDistributionId(distributionId);
        groupSessionModel.setDistributionMessage(businessDistributionMessage);
        groupSessionRepository.save(groupSessionModel);
    }

    @Override
    public GroupSessionData loadGroupSession(String s) {
        Optional<GroupSessionModel> optionalGroupSessionModel = groupSessionRepository.findById(s);
        if (optionalGroupSessionModel.isPresent()) {
            GroupSessionModel groupSessionModel = optionalGroupSessionModel.get();
            return new GroupSessionData(groupSessionModel.getName(),
                    groupSessionModel.getDeviceId(),
                    groupSessionModel.getDistributionId(),
                    groupSessionModel.getSessionRecord(),
                    groupSessionModel.getDistributionMessage());
        }
        return null;
    }

}

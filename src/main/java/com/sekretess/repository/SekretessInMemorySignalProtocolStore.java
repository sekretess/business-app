package com.sekretess.repository;

import com.sekretess.model.GroupSessionModel;
import com.sekretess.model.SessionModel;
import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.groups.state.SenderKeyRecord;
import org.signal.libsignal.protocol.state.SessionRecord;
import org.signal.libsignal.protocol.state.impl.InMemorySignalProtocolStore;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;


public class SekretessInMemorySignalProtocolStore extends InMemorySignalProtocolStore {

    private final SessionRepository sessionRepository;
    private final GroupSessionRepository groupSessionRepository;

    public SekretessInMemorySignalProtocolStore(IdentityKeyPair identityKeyPair, int registrationId, SessionRepository sessionRepository, GroupSessionRepository groupSessionRepository) {
        super(identityKeyPair, registrationId);
        this.sessionRepository = sessionRepository;
        this.groupSessionRepository = groupSessionRepository;
    }

    @Override
    public void storeSenderKey(SignalProtocolAddress sender, UUID distributionId, SenderKeyRecord record) {
        super.storeSenderKey(sender, distributionId, record);
        Optional<GroupSessionModel> optionalGroupSessionModel = groupSessionRepository.findById(sender.getName());
        GroupSessionModel groupSessionModel;
        groupSessionModel = optionalGroupSessionModel.orElseGet(GroupSessionModel::new);
        groupSessionModel.setName(sender.getName());
        groupSessionModel.setDeviceId(sender.getDeviceId());
        groupSessionModel.setSessionRecord(Base64.getEncoder().encodeToString(record.serialize()));
        groupSessionModel.setDistributionId(distributionId.toString());
        groupSessionRepository.save(groupSessionModel);
    }

    @Override
    public void storeSession(SignalProtocolAddress address, SessionRecord record) {
        super.storeSession(address, record);
        SessionModel sessionModel = new SessionModel();
        sessionModel.setName(address.getName());
        sessionModel.setDeviceId(address.getDeviceId());
        sessionModel.setSessionRecord(Base64.getEncoder().encodeToString(record.serialize()));
        sessionRepository.save(sessionModel);
    }

    @Override
    public void deleteSession(SignalProtocolAddress address) {
        super.deleteSession(address);
        sessionRepository.deleteById(address.getName());
    }
}

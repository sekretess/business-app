package com.sekretess.repository;

import com.sekretess.model.SessionModel;
import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.state.SessionRecord;
import org.signal.libsignal.protocol.state.impl.InMemorySignalProtocolStore;

import java.util.Base64;


public class SekretessInMemorySignalProtocolStore extends InMemorySignalProtocolStore {

    private final SessionRepository sessionRepository;

    public SekretessInMemorySignalProtocolStore(IdentityKeyPair identityKeyPair, int registrationId, SessionRepository sessionRepository) {
        super(identityKeyPair, registrationId);
        this.sessionRepository = sessionRepository;
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

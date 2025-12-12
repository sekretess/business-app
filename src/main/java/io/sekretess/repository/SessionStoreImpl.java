package io.sekretess.repository;

import io.sekretess.model.SessionData;
import io.sekretess.model.SessionModel;
import io.sekretess.store.SessionStore;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SessionStoreImpl implements SessionStore {
    private final SessionRepository sessionRepository;

    public SessionStoreImpl(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    public void saveSession(String consumerName, int deviceId, String base64EncodedRecord) {
        SessionModel sessionModel = new SessionModel();
        sessionModel.setName(consumerName);
        sessionModel.setDeviceId(deviceId);
        sessionModel.setSessionRecord(base64EncodedRecord);
        sessionRepository.save(sessionModel);
    }

    @Override
    public List<SessionData> loadAll() {
        return sessionRepository.findAll()
                .stream()
                .map(model -> new SessionData(
                        model.getName(),
                        model.getDeviceId(),
                        model.getSessionRecord()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteSession(String name) {
        sessionRepository.deleteById(name);
    }
}

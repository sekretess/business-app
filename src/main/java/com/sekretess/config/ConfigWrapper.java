package com.sekretess.config;

import com.sekretess.model.IdentityKeyModel;
import com.sekretess.model.SessionModel;
import com.sekretess.repository.IdentityKeyRepository;
import com.sekretess.repository.SekretessInMemorySignalProtocolStore;
import com.sekretess.repository.SessionRepository;
import org.signal.libsignal.protocol.IdentityKey;
import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.InvalidMessageException;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.ecc.Curve;
import org.signal.libsignal.protocol.ecc.ECKeyPair;
import org.signal.libsignal.protocol.state.SessionRecord;
import org.signal.libsignal.protocol.util.KeyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Configuration
public class ConfigWrapper {

    private final IdentityKeyRepository identityKeyRepository;
    private final SessionRepository sessionRepository;

    private final String username;
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigWrapper.class);

    public ConfigWrapper(IdentityKeyRepository identityKeyRepository, SessionRepository sessionRepository,
                         @Value("${app.config.username}") String username) {
        this.identityKeyRepository = identityKeyRepository;
        this.sessionRepository = sessionRepository;
        this.username = username;
    }

    @Bean
    public SekretessInMemorySignalProtocolStore sekretessInMemorySignalProtocolStore() {
        Optional<IdentityKeyModel> optionalIdentityKeyModel = identityKeyRepository.findById(username);
        if (optionalIdentityKeyModel.isEmpty()) {
            LOGGER.info("No identityKeys saved for the user: {}. Creating new one!", username);
            ECKeyPair ecKeyPair = Curve.generateKeyPair();
            IdentityKeyPair identityKeyPair = new IdentityKeyPair(new IdentityKey(ecKeyPair.getPublicKey()), ecKeyPair.getPrivateKey());
            int registrationId = KeyHelper.generateRegistrationId(false);
            IdentityKeyModel identityKeyModel = new IdentityKeyModel();
            identityKeyModel.setUserName(username);
            identityKeyModel.setIdentityKey(Base64.getEncoder().encodeToString(identityKeyPair.serialize()));
            identityKeyModel.setRegistrationId(registrationId);
            identityKeyRepository.save(identityKeyModel);
            return new SekretessInMemorySignalProtocolStore(identityKeyPair, registrationId, sessionRepository);
        } else {
            LOGGER.info("Found identityKeys for the user: {}. Will re-use it", username);
            String serializeIdentityKey = optionalIdentityKeyModel.get().getIdentityKey();
            IdentityKeyPair identityKeyPair = new IdentityKeyPair(Base64.getDecoder().decode(serializeIdentityKey));
            int registrationId = optionalIdentityKeyModel.get().getRegistrationId();

            SekretessInMemorySignalProtocolStore sekretessInMemorySignalProtocolStore = new SekretessInMemorySignalProtocolStore(identityKeyPair, registrationId, sessionRepository);
            List<SessionModel> sessionModels = sessionRepository.findAll();

            sessionModels.forEach(sessionModel -> {
                SignalProtocolAddress signalProtocolAddress = new SignalProtocolAddress(sessionModel.getName(), sessionModel.getDeviceId());
                SessionRecord sessionRecord = null;
                try {
                    sessionRecord = new SessionRecord(Base64.getDecoder().decode(sessionModel.getSessionRecord()));
                } catch (InvalidMessageException e) {
                    LOGGER.error("Exception happened when to create session record from DB! {}", e.getMessage(), e);
                }
                sekretessInMemorySignalProtocolStore.storeSession(signalProtocolAddress, sessionRecord);

            });

            return sekretessInMemorySignalProtocolStore;
        }
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder().build();
    }

}

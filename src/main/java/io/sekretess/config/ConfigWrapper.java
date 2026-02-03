package io.sekretess.config;

import io.sekretess.manager.SekretessManager;
import io.sekretess.manager.SekretessManagerFactory;
import io.sekretess.repository.GroupSessionStoreImpl;
import io.sekretess.repository.IdentityStoreImpl;
import io.sekretess.repository.SessionStoreImpl;
import org.signal.libsignal.protocol.InvalidKeyException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigWrapper {

    private final IdentityStoreImpl identityStore;
    private final SessionStoreImpl sessionStore;
    private final GroupSessionStoreImpl groupSessionStore;

    public ConfigWrapper(IdentityStoreImpl identityStore,
                         SessionStoreImpl sessionStore,
                         GroupSessionStoreImpl groupSessionStore) {
        this.identityStore = identityStore;
        this.sessionStore = sessionStore;
        this.groupSessionStore = groupSessionStore;
    }

    @Bean
    public SekretessManager sekretessManager() throws InvalidKeyException {
        return SekretessManagerFactory.createSekretessManager(identityStore, sessionStore, groupSessionStore);
    }
}
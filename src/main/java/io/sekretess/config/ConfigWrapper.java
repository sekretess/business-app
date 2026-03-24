package io.sekretess.config;

import io.sekretess.manager.SekretessManager;
import io.sekretess.manager.SekretessManagerFactory;
import io.sekretess.store.GroupSessionStore;
import io.sekretess.store.IdentityStore;
import io.sekretess.store.SessionStore;
import org.signal.libsignal.protocol.InvalidKeyException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigWrapper {

    private final IdentityStore identityStore;
    private final SessionStore sessionStore;
    private final GroupSessionStore groupSessionStore;

    public ConfigWrapper(IdentityStore identityStore,
                         SessionStore sessionStore,
                         GroupSessionStore groupSessionStore) {
        this.identityStore = identityStore;
        this.sessionStore = sessionStore;
        this.groupSessionStore = groupSessionStore;
    }

    @Bean
    public SekretessManager sekretessManager() throws InvalidKeyException {
        return SekretessManagerFactory.createSekretessManager(identityStore, sessionStore, groupSessionStore);
    }
}
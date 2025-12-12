package io.sekretess.config;

import io.sekretess.manager.SekretessManager;
import io.sekretess.repository.*;
import io.sekretess.store.SekretessSignalProtocolStore;
import io.sekretess.store.SekretessStoreFactory;
import org.signal.libsignal.protocol.*;
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
    public SekretessSignalProtocolStore sekretessInMemorySignalProtocolStore() throws InvalidKeyException {

        return SekretessStoreFactory.initialize(identityStore, sessionStore, groupSessionStore);
    }

    @Bean
    public SekretessManager sekretessManager(SekretessSignalProtocolStore store) throws InvalidKeyException {
        return new SekretessManager(store);
    }
}
package io.sekretess.repository;

import io.sekretess.model.IdentityKeyData;
import io.sekretess.model.IdentityKeyModel;
import io.sekretess.store.IdentityStore;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Optional;

@Component
public class IdentityStoreImpl implements IdentityStore {
    private final IdentityKeyRepository identityKeyRepository;

    public IdentityStoreImpl(IdentityKeyRepository identityKeyRepository) {
        this.identityKeyRepository = identityKeyRepository;
    }

    @Override
    public IdentityKeyData loadIdentity(String username) {
        Optional<IdentityKeyModel> optionalIdentityKeyModel = identityKeyRepository.findById(username);
        if (optionalIdentityKeyModel.isEmpty()) {
            return null;
        }
        IdentityKeyModel identityKeyModel = optionalIdentityKeyModel.get();
        return new IdentityKeyData(identityKeyModel.getUserName(), Base64.getDecoder().decode(identityKeyModel.getIdentityKey()), identityKeyModel.getRegistrationId());
    }

    @Override
    public void saveIdentity(String userName, byte[] bytes, int registrationId) {
        IdentityKeyModel identityKeyModel = new IdentityKeyModel();
        identityKeyModel.setUserName(userName);
        identityKeyModel.setIdentityKey(Base64.getEncoder().encodeToString(bytes));
        identityKeyModel.setRegistrationId(registrationId);
        identityKeyRepository.save(identityKeyModel);
    }
}

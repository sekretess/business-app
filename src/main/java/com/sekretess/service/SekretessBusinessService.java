package com.sekretess.service;

import com.sekretess.client.SekretessServerClient;
import com.sekretess.client.response.ConsumerKeysResponse;
import com.sekretess.dto.MessageDTO;
import com.sekretess.repository.SekretessInMemorySignalProtocolStore;
import org.signal.libsignal.protocol.*;
import org.signal.libsignal.protocol.ecc.ECPublicKey;
import org.signal.libsignal.protocol.message.CiphertextMessage;
import org.signal.libsignal.protocol.message.PreKeySignalMessage;
import org.signal.libsignal.protocol.state.PreKeyBundle;
import org.signal.libsignal.protocol.state.SessionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Base64;

@Service
public class SekretessBusinessService {
    private SekretessInMemorySignalProtocolStore sekretessInMemorySignalProtocolStore;
    private SekretessServerClient sekretessServerClient;
    private static final Logger logger = LoggerFactory.getLogger(SekretessBusinessService.class);

    private final String username;

    public SekretessBusinessService(SekretessInMemorySignalProtocolStore sekretessInMemorySignalProtocolStore,
                                    SekretessServerClient sekretessServerClient,
                                    @Value("${app.config.username}") String username) {
        this.sekretessInMemorySignalProtocolStore = sekretessInMemorySignalProtocolStore;
        this.sekretessServerClient = sekretessServerClient;
        this.username = username;
    }


    public void deleteSession(String user) {
        logger.info("Delete user session request received! {}", user);
        sekretessInMemorySignalProtocolStore.deleteSession(new SignalProtocolAddress(user, 123));
    }

    public void handleSendMessage(MessageDTO messageDTO) {
        logger.info("Send message request received to send message to consumer: {}", messageDTO.getConsumer());
        SignalProtocolAddress consumerAddress = new SignalProtocolAddress(messageDTO.getConsumer(), 123);
        SessionRecord sessionRecord = sekretessInMemorySignalProtocolStore.loadSession(consumerAddress);
        if (sessionRecord == null) {
            logger.info("No session available for consumer: {}", messageDTO.getConsumer());
            SessionBuilder sessionBuilder = new SessionBuilder(sekretessInMemorySignalProtocolStore, consumerAddress);
            PreKeyBundle consumerPrekeyBundle = getConsumerPrekeyBundle(messageDTO.getConsumer());
            try {
                sessionBuilder.process(consumerPrekeyBundle);
            } catch (InvalidKeyException | UntrustedIdentityException e) {
                logger.error("Exception happened when trying to create session with consumer: {} , {}", messageDTO.getConsumer(), e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }


        SessionCipher sessionCipher = new SessionCipher(sekretessInMemorySignalProtocolStore, consumerAddress);
        try {
            CiphertextMessage ciphertextMessage = sessionCipher.encrypt(messageDTO.getText().getBytes());
            PreKeySignalMessage signalMessage = new PreKeySignalMessage(ciphertextMessage.serialize());
            sekretessServerClient.sendMessage(username, Base64.getEncoder().encodeToString(signalMessage.serialize()), messageDTO.getConsumer());
        } catch (NoSessionException | UntrustedIdentityException | InvalidKeyException | InvalidVersionException |
                 InvalidMessageException | LegacyMessageException | IOException | InterruptedException e) {
            logger.error("Exception happened when trying to encrypt message! {}", e.getMessage(), e);
        }
    }


    private PreKeyBundle getConsumerPrekeyBundle(String consumer) {
        try {
            ConsumerKeysResponse consumerKeysResponse = sekretessServerClient.getConsumerKeys(consumer);
            String signedPreKey = consumerKeysResponse.getSpk();
            String[] preKeyRecords = consumerKeysResponse.getOpk().split(":");
            String preKeyRecordValue = preKeyRecords[1];
            int preKeyId = Integer.parseInt(preKeyRecords[0]);
            int regId = consumerKeysResponse.getRegId();
            String identityKey = consumerKeysResponse.getIk();
            int signedPreKeyId = Integer.parseInt(consumerKeysResponse.getSpkId());
            byte[] signedPreKeySignature = Base64.getDecoder().decode(consumerKeysResponse.getSpkSignature());
            ECPublicKey signPrekey = new ECPublicKey(Base64.getDecoder().decode(signedPreKey));
            ECPublicKey preKeyRecord = new ECPublicKey(Base64.getDecoder().decode(preKeyRecordValue));
            IdentityKey idenKey = new IdentityKey(Base64.getDecoder().decode(identityKey));
            return new PreKeyBundle(
                    regId,
                    1,
                    preKeyId,
                    preKeyRecord,
                    signedPreKeyId,
                    signPrekey,
                    signedPreKeySignature,
                    idenKey);

        } catch (IOException e) {
            logger.error("exception happened! {}",e.getMessage(),e);
        } catch (InterruptedException e) {
            logger.error("exception happened! {}",e.getMessage(),e);
        } catch (InvalidKeyException e) {
            logger.error("exception happened! {}",e.getMessage(),e);
        }

        return null;
    }

}

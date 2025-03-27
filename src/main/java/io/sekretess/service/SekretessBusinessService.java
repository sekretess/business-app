package io.sekretess.service;

import io.sekretess.client.SekretessServerClient;
import io.sekretess.client.response.ConsumerKeysResponse;
import io.sekretess.dto.AdsMessageDTO;
import io.sekretess.dto.MessageDTO;
import io.sekretess.exception.RetryMessageException;
import io.sekretess.model.GroupSessionModel;
import io.sekretess.repository.GroupSessionRepository;
import io.sekretess.repository.SekretessInMemorySignalProtocolStore;
import io.sekretess.util.MessageType;
import org.signal.libsignal.protocol.*;
import org.signal.libsignal.protocol.ecc.ECPublicKey;
import org.signal.libsignal.protocol.groups.GroupCipher;
import org.signal.libsignal.protocol.groups.GroupSessionBuilder;
import org.signal.libsignal.protocol.kem.KEMPublicKey;
import org.signal.libsignal.protocol.message.CiphertextMessage;
import org.signal.libsignal.protocol.message.PreKeySignalMessage;
import org.signal.libsignal.protocol.message.SenderKeyDistributionMessage;
import org.signal.libsignal.protocol.state.PreKeyBundle;
import org.signal.libsignal.protocol.state.SessionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

@Service
public class SekretessBusinessService {
    private final SekretessInMemorySignalProtocolStore sekretessInMemorySignalProtocolStore;
    private final SekretessServerClient sekretessServerClient;
    private final String username;
    private final GroupSessionRepository groupSessionRepository;
    private static final Logger logger = LoggerFactory.getLogger(SekretessBusinessService.class);


    public SekretessBusinessService(SekretessInMemorySignalProtocolStore sekretessInMemorySignalProtocolStore,
                                    SekretessServerClient sekretessServerClient,
                                    GroupSessionRepository groupSessionRepository,
                                    @Value("${app.config.username}") String username) {
        this.sekretessInMemorySignalProtocolStore = sekretessInMemorySignalProtocolStore;
        this.sekretessServerClient = sekretessServerClient;
        this.groupSessionRepository = groupSessionRepository;
        this.username = username;
    }


    public void deleteSession(String user) {
        logger.info("Delete user session request received! {}", user);
        sekretessInMemorySignalProtocolStore.deleteSession(new SignalProtocolAddress(user, 123));
    }


    private void handleRetrySendMessage(MessageDTO messageDTO) {
        logger.info("Received to retry message to consumer: {}", messageDTO.getConsumer());
        SignalProtocolAddress consumerAddress = new SignalProtocolAddress(messageDTO.getConsumer(), 123);
        SessionBuilder sessionBuilder = new SessionBuilder(sekretessInMemorySignalProtocolStore, consumerAddress);
        PreKeyBundle consumerPrekeyBundle = getConsumerPrekeyBundle(messageDTO.getConsumer());
        sekretessInMemorySignalProtocolStore.saveIdentity(consumerAddress, consumerPrekeyBundle.getIdentityKey());
        try {
            sessionBuilder.process(consumerPrekeyBundle);
            sekretessInMemorySignalProtocolStore.loadSession(consumerAddress);
        } catch (InvalidKeyException | UntrustedIdentityException e) {
            logger.error("Exception happened when trying to create session with consumer: {} , {}", messageDTO.getConsumer(), e.getMessage(), e);
            throw new RuntimeException(e);
        }

        SessionCipher sessionCipher = new SessionCipher(sekretessInMemorySignalProtocolStore, consumerAddress);
        try {
            CiphertextMessage ciphertextMessage = sessionCipher.encrypt(messageDTO.getText().getBytes());
            PreKeySignalMessage signalMessage = new PreKeySignalMessage(ciphertextMessage.serialize());
            sekretessServerClient.sendMessage(username, Base64.getEncoder().encodeToString(signalMessage.serialize()), messageDTO.getConsumer(), messageDTO.getType());
        } catch (Exception e) {
            logger.error("Exception happened when trying to send message! {}", e.getMessage(), e);
        }
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
                sessionRecord = sekretessInMemorySignalProtocolStore.loadSession(consumerAddress);
            } catch (InvalidKeyException | UntrustedIdentityException e) {
                logger.error("Exception happened when trying to create session with consumer: {} , {}", messageDTO.getConsumer(), e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }


        SessionCipher sessionCipher = new SessionCipher(sekretessInMemorySignalProtocolStore, consumerAddress);
        try {
            CiphertextMessage ciphertextMessage = sessionCipher.encrypt(messageDTO.getText().getBytes());
            PreKeySignalMessage signalMessage = new PreKeySignalMessage(ciphertextMessage.serialize());
            String identityKey = sekretessServerClient.sendMessage(username, Base64.getEncoder().encodeToString(signalMessage.serialize()), messageDTO.getConsumer(), messageDTO.getType());
            IdentityKey idenKey = new IdentityKey(Base64.getDecoder().decode(identityKey));
            if (!Arrays.equals(sessionRecord.getRemoteIdentityKey().getPublicKey().serialize(), idenKey.getPublicKey().serialize())) {
                sekretessInMemorySignalProtocolStore.deleteSession(consumerAddress);
                throw new RetryMessageException("User keys got updated need to resend message!");
            }
        } catch (RetryMessageException e) {
            logger.warn("Retry message request received! {}", e.getMessage());
            handleRetrySendMessage(messageDTO);
        } catch (Exception e) {
            logger.error("Exception happened when trying to send message! {}", e.getMessage(), e);
        }
    }


    public void handleSendAdsMessage(AdsMessageDTO adsMessageDTO) {
        try {
            GroupSessionModel groupSessionModel = groupSessionRepository.findById(username).orElseThrow();
            GroupCipher groupCipher = new GroupCipher(this.sekretessInMemorySignalProtocolStore, new SignalProtocolAddress(username, 1));
            CiphertextMessage ciphertextMessage =
                    groupCipher.encrypt(UUID.fromString(groupSessionModel.getDistributionId()), adsMessageDTO.getText().getBytes());
            sekretessServerClient.sendAdMessage(Base64.getEncoder().encodeToString(ciphertextMessage.serialize()), username);
        } catch (Exception e) {
            logger.error("Exception happened when sending ads message! {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }

    }

    public void sendSenderKeyDistributionMessage(String consumer) {
        logger.info("Request received to subscribe ads messages from consumer: {}", consumer);
        try {
            SenderKeyDistributionMessage sentBusinessDistributionMessage = null;
            GroupSessionModel groupSessionModel = groupSessionRepository.findById(username).orElseThrow();
            if (groupSessionModel.getSessionRecord() != null) {
                sentBusinessDistributionMessage =
                        new SenderKeyDistributionMessage(Base64.getDecoder().decode(groupSessionModel.getDistributionMessage()));
            } else {
                GroupSessionBuilder businessSessionBuilder = new GroupSessionBuilder(this.sekretessInMemorySignalProtocolStore);
                SignalProtocolAddress businessAddress = new SignalProtocolAddress(username, 1);
                sentBusinessDistributionMessage = businessSessionBuilder.create(businessAddress, UUID.fromString(groupSessionModel.getDistributionId()));
            }

            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setType(MessageType.KEY_DIST.name());
            messageDTO.setConsumer(consumer);
            messageDTO.setText(Base64.getEncoder().encodeToString(sentBusinessDistributionMessage.serialize()));
            handleSendMessage(messageDTO);

        } catch (Exception e) {
            logger.error("Exception happened when trying to send senderkeydistribution message! {}", e.getMessage(), e);
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
            String pqSignedPrekey = consumerKeysResponse.getPqspk();
            int pqSignedPrekeyId = Integer.parseInt(consumerKeysResponse.getPqspkID());
            byte[] pqSignedPrekeySignature = Base64.getDecoder().decode(consumerKeysResponse.getPqSpkSignature());

            ECPublicKey signPrekey = new ECPublicKey(Base64.getDecoder().decode(signedPreKey));
            ECPublicKey preKeyRecord = new ECPublicKey(Base64.getDecoder().decode(preKeyRecordValue));
            IdentityKey idenKey = new IdentityKey(Base64.getDecoder().decode(identityKey));
            KEMPublicKey kemPublicKey = new KEMPublicKey(Base64.getDecoder().decode(pqSignedPrekey));
            return new PreKeyBundle(
                    regId,
                    1,
                    preKeyId,
                    preKeyRecord,
                    signedPreKeyId,
                    signPrekey,
                    signedPreKeySignature,
                    idenKey,
                    pqSignedPrekeyId,
                    kemPublicKey,
                    pqSignedPrekeySignature);

        } catch (Exception e) {
            logger.error("Exception happened when creating prekeybundle! {}", e.getMessage(), e);
            throw new RuntimeException("Exception happened when creating prekeybundle! " + e.getMessage());
        }
    }

}

package io.sekretess.config;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.impl.CredentialsProvider;
import io.sekretess.model.GroupSessionModel;
import io.sekretess.model.IdentityKeyModel;
import io.sekretess.model.SessionModel;
import io.sekretess.repository.GroupSessionRepository;
import io.sekretess.repository.IdentityKeyRepository;
import io.sekretess.repository.SekretessInMemorySignalProtocolStore;
import io.sekretess.repository.SessionRepository;
import io.sekretess.util.TokenProvider;
import org.signal.libsignal.protocol.IdentityKey;
import org.signal.libsignal.protocol.IdentityKeyPair;
import org.signal.libsignal.protocol.InvalidMessageException;
import org.signal.libsignal.protocol.SignalProtocolAddress;
import org.signal.libsignal.protocol.ecc.ECKeyPair;
import org.signal.libsignal.protocol.groups.GroupSessionBuilder;
import org.signal.libsignal.protocol.groups.state.SenderKeyRecord;
import org.signal.libsignal.protocol.message.SenderKeyDistributionMessage;
import org.signal.libsignal.protocol.state.SessionRecord;
import org.signal.libsignal.protocol.util.KeyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Configuration
public class ConfigWrapper {

    private final IdentityKeyRepository identityKeyRepository;
    private final SessionRepository sessionRepository;
    private final GroupSessionRepository groupSessionRepository;
    private final String username;
    private final TokenProvider tokenProvider;
    private final String rabbitMQHost;
    private final int rabbitMQPort;
    private final String rabbitMqVhost;
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigWrapper.class);

    public ConfigWrapper(IdentityKeyRepository identityKeyRepository,
                         SessionRepository sessionRepository,
                         GroupSessionRepository groupSessionRepository,
                         TokenProvider tokenProvider,
                         @Value("${app.config.username}") String username,
                         @Value("${app.config.rabbitmq.host}") String rabbitMQHost,
                         @Value("${app.config.rabbitmq.port}") int rabbitMQPort,
                         @Value("${app.config.rabbitmq.vhost}") String rabbitMqVhost) {
        this.identityKeyRepository = identityKeyRepository;
        this.sessionRepository = sessionRepository;
        this.groupSessionRepository = groupSessionRepository;
        this.username = username;
        this.tokenProvider = tokenProvider;
        this.rabbitMQHost = rabbitMQHost;
        this.rabbitMQPort = rabbitMQPort;
        this.rabbitMqVhost = rabbitMqVhost;

    }

    @Bean
    public SekretessInMemorySignalProtocolStore sekretessInMemorySignalProtocolStore() {
        Optional<IdentityKeyModel> optionalIdentityKeyModel = identityKeyRepository.findById(username);
        if (optionalIdentityKeyModel.isEmpty()) {
            LOGGER.info("No identityKeys saved for the user: {}. Creating new one!", username);
            ECKeyPair ecKeyPair = ECKeyPair.generate();
            IdentityKeyPair identityKeyPair = new IdentityKeyPair(new IdentityKey(ecKeyPair.getPublicKey()), ecKeyPair.getPrivateKey());
            int registrationId = KeyHelper.generateRegistrationId(false);
            IdentityKeyModel identityKeyModel = new IdentityKeyModel();
            identityKeyModel.setUserName(username);
            identityKeyModel.setIdentityKey(Base64.getEncoder().encodeToString(identityKeyPair.serialize()));
            identityKeyModel.setRegistrationId(registrationId);
            identityKeyRepository.save(identityKeyModel);

            SekretessInMemorySignalProtocolStore sekretessInMemorySignalProtocolStore = new SekretessInMemorySignalProtocolStore(identityKeyPair, registrationId, sessionRepository, groupSessionRepository);
            GroupSessionBuilder businessSessionBuilder = new GroupSessionBuilder(sekretessInMemorySignalProtocolStore);
            SignalProtocolAddress businessAddress = new SignalProtocolAddress(username, 1);
            String distributionId = UUID.randomUUID().toString();
            SenderKeyDistributionMessage sentBusinessDistributionMessage = businessSessionBuilder.create(businessAddress, UUID.fromString(distributionId));
            Optional<GroupSessionModel> optionalGroupSessionModel = groupSessionRepository.findById(username);
            GroupSessionModel groupSessionModel;
            groupSessionModel = optionalGroupSessionModel.orElseGet(GroupSessionModel::new);
            groupSessionModel.setDistributionId(distributionId);
            groupSessionModel.setName(username);
            groupSessionModel.setDeviceId(1);
            groupSessionModel.setDistributionMessage(Base64.getEncoder().encodeToString(sentBusinessDistributionMessage.serialize()));
            groupSessionRepository.save(groupSessionModel);
            return sekretessInMemorySignalProtocolStore;
        } else {
            LOGGER.info("Found identityKeys for the user: {}. Will re-use it", username);
            String serializeIdentityKey = optionalIdentityKeyModel.get().getIdentityKey();
            IdentityKeyPair identityKeyPair = new IdentityKeyPair(Base64.getDecoder().decode(serializeIdentityKey));
            int registrationId = optionalIdentityKeyModel.get().getRegistrationId();
            SekretessInMemorySignalProtocolStore sekretessInMemorySignalProtocolStore = new SekretessInMemorySignalProtocolStore(identityKeyPair, registrationId, sessionRepository, groupSessionRepository);
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

            List<GroupSessionModel> groupSessionModels = groupSessionRepository.findAll();
            groupSessionModels.forEach(groupSessionModel -> {
                SignalProtocolAddress signalProtocolAddress = new SignalProtocolAddress(groupSessionModel.getName(), groupSessionModel.getDeviceId());
                SenderKeyRecord senderKeyRecord = null;
                try {
                    senderKeyRecord = new SenderKeyRecord(Base64.getDecoder().decode(groupSessionModel.getSessionRecord()));
                } catch (Exception e) {
                    LOGGER.error("Exception happened when creating senderKeyRecord! {}", e.getMessage(), e);
                }

                sekretessInMemorySignalProtocolStore.storeSenderKey(signalProtocolAddress, UUID.fromString(groupSessionModel.getDistributionId()), senderKeyRecord);
            });

            return sekretessInMemorySignalProtocolStore;
        }
    }

    @Bean
    public CachingConnectionFactory connectionFactory() throws NoSuchAlgorithmException, KeyManagementException {
        ConnectionFactory rabbitFactory = new ConnectionFactory();
        rabbitFactory.setHost(this.rabbitMQHost);
        rabbitFactory.setPort(this.rabbitMQPort);
        rabbitFactory.setVirtualHost(this.rabbitMqVhost);
        rabbitFactory.useSslProtocol();
        rabbitFactory.setCredentialsProvider(new CredentialsProvider() {
            @Override
            public String getUsername() {
                return tokenProvider.getUserName();
            }

            @Override
            public String getPassword() {
                return tokenProvider.fetchToken();
            }
        });

        return new CachingConnectionFactory(rabbitFactory);
    }

    @Bean
    public String queueName() {
        return username + "_business";
    }
}
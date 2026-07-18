package io.sekretess.service;

import io.sekretess.dto.AdsMessageDTO;
import io.sekretess.dto.MessageDTO;
import io.sekretess.exception.MessageProcessingException;
import io.sekretess.exception.MessageSendException;
import io.sekretess.exception.PrekeyBundleException;
import io.sekretess.exception.SessionCreationException;
import io.sekretess.manager.SekretessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

@Service
public class SekretessBusinessService {
    private final SekretessManager sekretessManager;
    private final Path uploadTempDirectory;
    private static final Logger logger = LoggerFactory.getLogger(SekretessBusinessService.class);


    @Autowired
    public SekretessBusinessService(SekretessManager sekretessManager) {
        this(sekretessManager, initializeUploadTempDirectory());
    }

    SekretessBusinessService(SekretessManager sekretessManager, Path uploadTempDirectory) {
        this.sekretessManager = sekretessManager;
        this.uploadTempDirectory = uploadTempDirectory;
    }

    public void handleSendMessage(MessageDTO messageDTO) {
        logger.info("Send message request received to send message to consumer: {}", messageDTO.getConsumer());
        try {
            this.sekretessManager.sendMessageToConsumer(messageDTO.getText(), messageDTO.getConsumer());
        } catch (SessionCreationException e) {
            throw new MessageProcessingException("Failed to create session for consumer: " + messageDTO.getConsumer(), e);
        } catch (PrekeyBundleException e) {
            throw new MessageProcessingException("Failed to retrieve prekey bundle for consumer: " + messageDTO.getConsumer(), e);
        } catch (MessageSendException e) {
            throw new MessageProcessingException("Failed to send message to consumer: " + messageDTO.getConsumer(), e);
        }
    }

    public void handleSendFile(MultipartFile file, String consumer) {
        validateFileRequest(file, consumer);
        logger.info("Send file request received to send file to consumer: {}", consumer);

        Path tempFile = null;
        try {
            tempFile = createTempFile(file);
            file.transferTo(tempFile);
            this.sekretessManager.sendFileToConsumer(tempFile, consumer);
        } catch (SessionCreationException e) {
            throw new MessageProcessingException("Failed to create session for consumer: " + consumer, e);
        } catch (PrekeyBundleException e) {
            throw new MessageProcessingException("Failed to retrieve prekey bundle for consumer: " + consumer, e);
        } catch (MessageSendException e) {
            throw new MessageProcessingException("Failed to send file to consumer: " + consumer, e);
        } catch (IOException e) {
            throw new MessageProcessingException("Failed to process uploaded file for consumer: " + consumer, e);
        } finally {
            deleteTempFile(tempFile);
        }
    }


    public void handleSendAdsMessage(AdsMessageDTO adsMessageDTO) {
        logger.info("Send ads message request received to send ads message to consumers!");
        try {
            this.sekretessManager.sendAdsMessage(adsMessageDTO.getText());
        } catch (MessageSendException e) {
            throw new MessageProcessingException("Failed to send ads message", e);
        }
    }

    private void validateFileRequest(MultipartFile file, String consumer) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        if (consumer == null || consumer.isBlank()) {
            throw new IllegalArgumentException("Consumer cannot be blank");
        }
    }

    private Path createTempFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String suffix = null;
        if (originalFilename != null) {
            int extensionIndex = originalFilename.lastIndexOf('.');
            if (extensionIndex >= 0) {
                suffix = originalFilename.substring(extensionIndex);
            }
        }

        if (Files.getFileAttributeView(uploadTempDirectory, PosixFileAttributeView.class) != null) {
            FileAttribute<Set<PosixFilePermission>> permissions =
                    PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------"));
            return Files.createTempFile(uploadTempDirectory, "upload-", suffix, permissions);
        }

        return Files.createTempFile(uploadTempDirectory, "upload-", suffix);
    }

    private void deleteTempFile(Path tempFile) {
        if (tempFile == null) {
            return;
        }
        try {
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            logger.warn("Failed to delete temp uploaded file: {}", tempFile, e);
        }
    }

    private static Path initializeUploadTempDirectory() {
        try {
            Path systemTempDirectory = Path.of(System.getProperty("java.io.tmpdir"));
            if (Files.getFileAttributeView(systemTempDirectory, PosixFileAttributeView.class) != null) {
                FileAttribute<Set<PosixFilePermission>> permissions =
                        PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));
                return Files.createTempDirectory(systemTempDirectory, "sekretess-upload-", permissions);
            }
            return Files.createTempDirectory(systemTempDirectory, "sekretess-upload-");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize secure upload temp directory", e);
        }
    }

}

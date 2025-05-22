package io.sekretess.config;

import io.sekretess.util.PasswordGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.FileReader;
import java.net.http.HttpClient;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

@Configuration
public class HttpClientConfigs {

    private final String certPath;
    private final String keyPath;
    private final String keyPassword;

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public HttpClientConfigs(@Value("${app.config.idp.certificate}") String certPath,
                             @Value("${app.config.idp.key}") String keyPath,
                             @Value("${app.config.idp.password}") String keyPassword) {
        this.certPath = certPath;
        this.keyPath = keyPath;
        this.keyPassword = keyPassword;
    }

    private SSLContext createSslContext() {
        try {
            X509Certificate certificate;
            try (FileInputStream fis = new FileInputStream(certPath)) {
                CertificateFactory factory = CertificateFactory.getInstance("X.509");
                certificate = (X509Certificate) factory.generateCertificate(fis);
            }

            PrivateKey privateKey;
            try (PEMParser pemParser = new PEMParser(new FileReader(keyPath))) {
                Object object = pemParser.readObject();
                if (object instanceof PKCS8EncryptedPrivateKeyInfo) {
                    PKCS8EncryptedPrivateKeyInfo encryptedPrivateKeyInfo = (PKCS8EncryptedPrivateKeyInfo) object;
                    InputDecryptorProvider decryptorProvider =
                            new JceOpenSSLPKCS8DecryptorProviderBuilder()
                                    .build(keyPassword.toCharArray());
                    privateKey = new JcaPEMKeyConverter().setProvider("BC")
                            .getPrivateKey(encryptedPrivateKeyInfo.decryptPrivateKeyInfo(decryptorProvider));

                } else {
                    privateKey = new JcaPEMKeyConverter().setProvider("BC").getKeyPair((PEMKeyPair) object).getPrivate();
                }
            }

            char[] keyStorePassword = PasswordGenerator.generatePassword();
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(null, null);
            keyStore.setKeyEntry("client", privateKey, keyStorePassword, new Certificate[]{certificate});

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, keyStorePassword);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());

            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSLContext", e);
        }
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder().build();
    }


    @Bean(name = "mtlsHttpClient")
    public HttpClient mtlsHttpClient() {
        return HttpClient.newBuilder().sslContext(createSslContext()).build();
    }
}

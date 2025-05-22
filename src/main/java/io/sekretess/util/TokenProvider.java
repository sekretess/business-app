package io.sekretess.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class TokenProvider {

    private final String idpUrl;
    private final String clientId;
    private final HttpClient httpClient;
    private final Cache<String, String> cache = Caffeine.newBuilder()
            .expireAfterWrite(22, TimeUnit.MINUTES)
            .build();


    public TokenProvider(@Value("${app.config.idp.url}") String idpUrl,
                         @Value("${app.config.idp.clientId}") String clientId,
                         @Qualifier("mtlsHttpClient") HttpClient httpClient) {
        this.idpUrl = idpUrl;
        this.clientId = clientId;
        this.httpClient = httpClient;
    }

    public String getUserName() {
        String token = cache.get(clientId, k -> getToken());
        if (token == null) {
            throw new RuntimeException("Failed to get token!");
        }
        DecodedJWT jwt = JWT.decode(token);
        return jwt.getClaim("preferred_username").asString();
    }


    private String getToken() {
        String body = "client_id=" + clientId + "&grant_type=password";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(idpUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to get token: " + response.body());
        }
        Gson gson = new Gson();
        return gson.fromJson(response.body(), TokenObject.class).access_token;
    }

    public String fetchToken() {
        String token = cache.get(clientId, k -> getToken());
        if (token == null) {
            throw new RuntimeException("Failed to get token!");
        }
        DecodedJWT jwt = JWT.decode(token);
        Date expiration = jwt.getExpiresAt();
        if (expiration.before(new Date())) {
            cache.invalidate(clientId);
            token = getToken();
            cache.put(clientId, token);

        }
        return token;
    }

    private static class TokenObject {
        private String access_token;

        public String getAccess_token() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }
    }
}

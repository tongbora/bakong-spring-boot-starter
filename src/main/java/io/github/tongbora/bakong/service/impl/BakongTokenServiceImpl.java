package io.github.tongbora.bakong.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tongbora.bakong.config.BakongProperties;
import io.github.tongbora.bakong.service.BakongTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;

@Slf4j
public class BakongTokenServiceImpl implements BakongTokenService {

    private final RestClient restClient;
    private final ObjectMapper mapper;
    private final BakongProperties properties;

    private String cachedToken;
    private Instant tokenExpiry;

    public BakongTokenServiceImpl(RestClient restClient, ObjectMapper mapper, BakongProperties properties) {
        this.restClient = restClient;
        this.mapper = mapper;
        this.properties = properties;
    }

    @Override
    public synchronized String getToken() {
        if (cachedToken != null && tokenExpiry != null && Instant.now().isBefore(tokenExpiry)) {
            log.info("Using cached token");
            return cachedToken;
        }

        log.info("Renewing token from Bakong");

        String url = properties.getBaseUrl().replaceAll("/+$", "") + "/v1/renew_token";

        String responseBody = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("email", properties.getEmail()))
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = mapper.readTree(responseBody);
            JsonNode tokenNode = root.path("data").path("token");

            if (tokenNode.isMissingNode() || tokenNode.isNull()) {
                throw new RuntimeException("Bakong token not returned");
            }

            cachedToken = tokenNode.asText();

            String[] parts = cachedToken.split("\\.");
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            JsonNode payloadNode = mapper.readTree(payload);

            long exp = payloadNode.path("exp").asLong();
            tokenExpiry = Instant.ofEpochSecond(exp);

            log.info("Obtained new token, expires at {}", tokenExpiry);

            return cachedToken;

        } catch (Exception e) {
            throw new RuntimeException("Failed to obtain Bakong token", e);
        }
    }
}
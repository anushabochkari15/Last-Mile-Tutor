package com.example.maths.service;

import com.example.maths.config.LlmProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

@Service
public class LlmClient {

    private static final Logger log = LoggerFactory.getLogger(LlmClient.class);

    private final LlmProperties props;
    private final ObjectMapper objectMapper;

    public LlmClient(LlmProperties props, ObjectMapper objectMapper) {
        this.props = props;
        this.objectMapper = objectMapper;
        String key = props.getApiKey();
        if (key == null || key.isBlank()) {
            log.warn("LLM_API_KEY is not set; AI hints and lessons will use built-in content.");
        }
    }

    public Optional<String> complete(String systemPrompt, String userPrompt) {
        String apiKey = props.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }
        if (!"anthropic".equals(props.getProvider())) {
            return Optional.empty();
        }
        try {
            Duration timeout = Duration.ofSeconds(props.getTimeoutSeconds());
            HttpClient client = HttpClient.newBuilder().connectTimeout(timeout).build();

            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", props.getModel());
            body.put("max_tokens", 600);
            body.put("system", systemPrompt);
            ArrayNode messages = body.putArray("messages");
            ObjectNode message = messages.addObject();
            message.put("role", "user");
            message.put("content", userPrompt);
            String json = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder(URI.create(props.getBaseUrl() + "/v1/messages"))
                    .timeout(timeout)
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status < 200 || status >= 300) {
                log.warn("LLM request failed with HTTP status {}", status);
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode first = root.path("content").path(0);
            JsonNode type = first.get("type");
            if (type == null || !"text".equals(type.asText())) {
                return Optional.empty();
            }
            JsonNode text = first.get("text");
            if (text == null || !text.isTextual() || text.asText().isBlank()) {
                return Optional.empty();
            }
            return Optional.of(text.asText());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("LLM request interrupted: {}", e.getClass().getName());
            return Optional.empty();
        } catch (IOException e) {
            log.warn("LLM request failed: {}", e.getClass().getName());
            return Optional.empty();
        } catch (RuntimeException e) {
            log.warn("LLM request failed: {}", e.getClass().getName());
            return Optional.empty();
        }
    }
}

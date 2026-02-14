package com.example.assistant.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Service
public class OllamaClient {
    private final WebClient client = WebClient.builder().baseUrl("http://localhost:11434").build();
    private final String model = "pdf-assistant";

    public String generate(String prompt) {
        Map<String, Object> body = Map.of(
                "model", model,
                "prompt", prompt,
                "stream", false);
        try {
            var resp = client.post()
                    .uri("/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(30))
                    .onErrorResume(e -> Mono.just(Map.of("response", "")))
                    .block();
            Object r = resp == null ? "" : resp.get("response");
            return r == null ? "" : r.toString();
        } catch (Exception e) {
            return "";
        }
    }
}

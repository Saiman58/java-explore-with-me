package ru.practicum.explorewithme.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.explorewithme.stats.dto.EndpointHit;
import ru.practicum.explorewithme.stats.dto.ViewStats;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class StatClient {
    private final RestTemplateBuilder builder;
    private RestTemplate restTemplate;
    @Value("${stats-server.url:http://localhost:9090}")
    private String serverUrl;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatClient(RestTemplateBuilder builder) {
        this.builder = builder;
    }

    @PostConstruct
    public void init() {
        log.info("Инициализация StatClient, serverUrl: {}", serverUrl);
        if (serverUrl == null || serverUrl.isEmpty()) {
            throw new IllegalStateException("stats-server.url не загружен! Проверь application-local.yml");
        }
        this.restTemplate = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .build();
        log.info("StatClient инициализирован с URL: {}", serverUrl);
    }

    public void postHit(EndpointHit hit) {
        try {
            restTemplate.postForObject("/hit", hit, EndpointHit.class);
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Ошибка при сохранении статистики: " + e.getStatusCode());
        }
    }

    public ResponseEntity<List<ViewStats>> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("start", start.format(formatter));
        parameters.put("end", end.format(formatter));
        parameters.put("uris", uris != null ? uris : List.of());
        parameters.put("unique", unique);

        return restTemplate.exchange(
                "/stats?start={start}&end={end}&uris={uris}&unique={unique}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ViewStats>>() {},
                parameters
        );
    }
}
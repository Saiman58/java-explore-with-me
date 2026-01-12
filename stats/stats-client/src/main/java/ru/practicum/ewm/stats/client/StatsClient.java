package ru.practicum.ewm.stats.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatsClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${stats.server.url}")
    private String serverUrl;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public void saveHit(EndpointHitDto hit) {
        log.info("Sending hit to stats server: {}", hit);
        try {
            restTemplate.postForEntity(serverUrl + "/hit", hit, Void.class);
        } catch (Exception e) {
            log.error("Failed to send hit: {}", e.getMessage());
        }
    }

    public List<ViewStatsDto> getStats(LocalDateTime start,
                                       LocalDateTime end,
                                       boolean unique,
                                       List<String> uris) {

        var uriBuilder = UriComponentsBuilder
                .fromHttpUrl(serverUrl + "/stats")
                .queryParam("start", start.format(FORMATTER))
                .queryParam("end", end.format(FORMATTER))
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            uris.forEach(u -> uriBuilder.queryParam("uris", u));
        }

        String url = uriBuilder.toUriString();
        log.info("Requesting stats from server: {}", url);

        try {
            ResponseEntity<ViewStatsDto[]> response =
                    restTemplate.getForEntity(url, ViewStatsDto[].class);

            log.info("Received stats, count={}",
                    response.getBody() != null ? response.getBody().length : 0);

            return response.getBody() != null ? List.of(response.getBody()) : List.of();
        } catch (Exception e) {
            log.error("Failed to fetch stats: {}", e.getMessage());
            return List.of();
        }
    }
}

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
        log.debug("Отправка статистики в сервис: app={}, uri={}, ip={}, time={}",
                hit.getApp(), hit.getUri(), hit.getIp(), hit.getTimestamp());

        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    serverUrl + "/hit",
                    hit,
                    Void.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("Статистика успешно отправлена в сервис: app={}, uri={}",
                        hit.getApp(), hit.getUri());
            } else {
                log.warn("Сервис статистики вернул неожиданный статус: {}",
                        response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Ошибка при отправке статистики в сервис: {}", e.getMessage(), e);
        }
    }

    public List<ViewStatsDto> getStats(LocalDateTime start,
                                       LocalDateTime end,
                                       boolean unique,
                                       List<String> uris) {

        log.debug("Запрос статистики: период с {} по {}, уникальные={}, uris={}",
                start, end, unique, uris);

        var uriBuilder = UriComponentsBuilder
                .fromHttpUrl(serverUrl + "/stats")
                .queryParam("start", start.format(FORMATTER))
                .queryParam("end", end.format(FORMATTER))
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            uris.forEach(u -> uriBuilder.queryParam("uris", u));
        }

        String url = uriBuilder.toUriString();
        log.debug("Сформированный URL для запроса статистики: {}", url);

        try {
            ResponseEntity<ViewStatsDto[]> response =
                    restTemplate.getForEntity(url, ViewStatsDto[].class);

            log.debug("Получен ответ от сервиса статистики: статус={}, количество записей={}",
                    response.getStatusCode(),
                    response.getBody() != null ? response.getBody().length : 0);

            return response.getBody() != null ? List.of(response.getBody()) : List.of();
        } catch (Exception e) {
            log.error("Ошибка при запросе статистики из сервиса: {}", e.getMessage(), e);
            log.warn("Возвращаем пустой список статистики из-за ошибки");
            return List.of();
        }
    }
}
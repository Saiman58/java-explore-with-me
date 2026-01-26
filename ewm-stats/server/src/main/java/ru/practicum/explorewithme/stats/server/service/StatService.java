package ru.practicum.explorewithme.stats.server.service;

import ru.practicum.explorewithme.stats.dto.EndpointHit;
import ru.practicum.explorewithme.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatService {

    // Сохранить информацию о запросе
    EndpointHit saveHit(EndpointHit hit);

    // Получить статистику
    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique);
}
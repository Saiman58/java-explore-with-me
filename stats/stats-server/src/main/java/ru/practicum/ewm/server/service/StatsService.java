package ru.practicum.ewm.server.service;

import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    EndpointHitDto save(EndpointHitDto dto);


    List<ViewStatsDto> getStats(LocalDateTime start,
                                LocalDateTime end,
                                List<String> uris,
                                Boolean unique);
}

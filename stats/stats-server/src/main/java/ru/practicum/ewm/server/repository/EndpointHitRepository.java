package ru.practicum.ewm.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.stats.dto.ViewStatsDto;
import ru.practicum.ewm.server.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface EndpointHitRepository extends JpaRepository<EndpointHit, Long> {

    // Получает общую статистику - считает ВСЕ хиты (включая дубли с одного IP)
    @Query("select new ru.practicum.ewm.stats.dto.ViewStatsDto(h.app, h.uri, count(h)) " +
            "from EndpointHit h " +
            "where h.timestamp between :start and :end " +
            "group by h.app, h.uri " +
            "order by count(h) desc")
    List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end);

    // Получает статистику по УНИКАЛЬНЫМ посетителям - считает только разные IP-адреса
    @Query("select new ru.practicum.ewm.stats.dto.ViewStatsDto(h.app, h.uri, count(distinct h.ip)) " +
            "from EndpointHit h " +
            "where h.timestamp between :start and :end " +
            "group by h.app, h.uri " +
            "order by count(distinct h.ip) desc")
    List<ViewStatsDto> getUniqueStats(LocalDateTime start, LocalDateTime end);
}

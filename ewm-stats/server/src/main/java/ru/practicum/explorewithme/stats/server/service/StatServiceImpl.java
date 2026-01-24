package ru.practicum.explorewithme.stats.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.stats.dto.EndpointHit;
import ru.practicum.explorewithme.stats.dto.ViewStats;
import ru.practicum.explorewithme.stats.server.entity.Hit;
import ru.practicum.explorewithme.stats.server.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {

    private final HitRepository repository;

    @Override
    public EndpointHit saveHit(EndpointHit hit) {
        log.debug("[StatService] Сохранение статистики: app={}, uri={}, ip={}",
                hit.getApp(), hit.getUri(), hit.getIp());

        repository.save(Hit.fromDto(hit));

        log.debug("[StatService] Статистика сохранена: app={}, uri={}",
                hit.getApp(), hit.getUri());
        return hit;
    }

    @Override
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        log.debug("[StatService] Получение статистики: start={}, end={}, uris={}, unique={}",
                start, end, uris, unique);

        List<ViewStats> stats;
        if (unique) {
            stats = repository.findUniqueStats(start, end, uris);
            log.debug("[StatService] Получена уникальная статистика: {} записей", stats.size());
        } else {
            stats = repository.findStats(start, end, uris);
            log.debug("[StatService] Получена полная статистика: {} записей", stats.size());
        }

        return stats;
    }
}
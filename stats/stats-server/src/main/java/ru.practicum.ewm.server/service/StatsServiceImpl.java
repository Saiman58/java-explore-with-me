package ru.practicum.ewm.server.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.server.mapper.EndpointHitMapper;
import ru.practicum.ewm.server.repository.StatsRepository;
import ru.practicum.ewm.server.repository.EndpointHitRepository;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;
import ru.practicum.ewm.server.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StatsServiceImpl implements StatsService {
    private static final Logger log = LoggerFactory.getLogger(StatsServiceImpl.class);


    private final EndpointHitRepository repository;
    private final StatsRepository statsRepository;
    private final EndpointHitMapper mapper;

    @Override
    public EndpointHitDto save(EndpointHitDto dto) {
        log.debug("Сохранение записи статистики: app={}, uri={}", dto.getApp(), dto.getUri());
        EndpointHit entity = mapper.toEntity(dto);
        EndpointHit saved = repository.save(entity);
        EndpointHitDto savedDto = mapper.toDto(saved);
        log.debug("Запись сохранена с ID={}", savedDto.getId());
        return savedDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStatsDto> getStats(LocalDateTime start,
                                       LocalDateTime end,
                                       List<String> uris,
                                       Boolean unique) {
        log.debug("Поиск статистики: start={}, end={}, uris={}, unique={}",
                start, end, uris, unique);

        return Boolean.TRUE.equals(unique)
                ? statsRepository.findStatsUnique(start, end, uris)
                : statsRepository.findStats(start, end, uris);
    }
}

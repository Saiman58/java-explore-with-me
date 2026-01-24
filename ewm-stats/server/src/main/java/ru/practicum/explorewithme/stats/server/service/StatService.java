package ru.practicum.explorewithme.stats.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.stats.dto.EndpointHit;
import ru.practicum.explorewithme.stats.dto.ViewStats;
import ru.practicum.explorewithme.stats.server.entity.Hit;
import ru.practicum.explorewithme.stats.server.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatService {
    private final HitRepository repository;

    public EndpointHit saveHit(EndpointHit hit) {
        repository.save(Hit.fromDto(hit));
        return hit;
    }

    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (unique) {
            return repository.findUniqueStats(start, end, uris);
        } else {
            return repository.findStats(start, end, uris);
        }
    }
}
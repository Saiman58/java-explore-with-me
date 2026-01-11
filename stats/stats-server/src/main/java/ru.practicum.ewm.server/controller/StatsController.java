package controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.StatsRequestDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;
import ru.practicum.ewm.server.service.StatsService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping
public class StatsController {

    private final StatsService service;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHitDto save(@RequestBody @Valid EndpointHitDto hitDto) {
        log.info("Получен новый запрос на сохранение статистики: сервиса= {}, URI= {}, IP= {}, время= {}",
                hitDto.getApp(), hitDto.getUri(), hitDto.getIp(), hitDto.getTimestamp());

        EndpointHitDto savedHit = service.save(hitDto);

        log.info("Статистика успешно сохранена с ID={}", savedHit.getId());
        return savedHit;
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(@Valid @ModelAttribute StatsRequestDto request) {

        log.info("Запрос статистики: период с {} по {}, URI: {}, уникальные посещения: {}",
                request.getStart(), request.getEnd(), request.getUris(), request.getUnique());

        List<ViewStatsDto> stats = service.getStats(
                request.getStart(),
                request.getEnd(),
                request.getUris(),
                request.getUnique()
        );

        log.info("Возвращено {} записей статистики", stats.size());
        return stats;
    }

}

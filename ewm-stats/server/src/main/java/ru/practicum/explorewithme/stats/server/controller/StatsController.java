package ru.practicum.explorewithme.stats.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.stats.dto.Constants;
import ru.practicum.explorewithme.stats.dto.EndpointHit;
import ru.practicum.explorewithme.stats.dto.ViewStats;
import ru.practicum.explorewithme.stats.server.service.StatService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class StatsController {
    private final StatService statService;

    @PostMapping("/hit")
    public ResponseEntity<EndpointHit> hit(@Valid @RequestBody EndpointHit endpointHit) {
        log.debug("Saving hit: {}", endpointHit);
        EndpointHit savedHit = statService.saveHit(endpointHit);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedHit);
    }

    @GetMapping("/stats")
    public ResponseEntity<List<ViewStats>> getStats(@RequestParam @DateTimeFormat(pattern = Constants.FORMATTER) LocalDateTime start,
                                                    @RequestParam @DateTimeFormat(pattern = Constants.FORMATTER) LocalDateTime end,
                                                    @RequestParam(required = false) List<String> uris,
                                                    @RequestParam(defaultValue = "false") boolean unique) {
        log.debug("Getting stats from {} to {}, uris: {}, unique: {}", start, end, uris, unique);

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Неверный диапазон дат: start не может быть после end");
        }

        List<ViewStats> stats = statService.getStats(start, end, uris, unique);
        return ResponseEntity.ok(stats);
    }
}
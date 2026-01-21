package ru.practicum.explorewithme.server.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.server.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.server.service.CompilationService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class PublicCompilationController {

    private final CompilationService compilationService;

    // GET /compilations
    // Получение подборок событий с возможностью фильтрации по закреплению
    @GetMapping
    public List<CompilationDto> getCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("GET /compilations - pinned={}, пагинация: from={}, size={}", pinned, from, size);
        return compilationService.getCompilations(pinned, from, size);
    }

    // GET /compilations/{compId}
    // Получение подборки событий по её идентификатору
    @GetMapping("/{compId}")
    public CompilationDto getCompilation(@PathVariable @Positive Long compId) {
        log.info("GET /compilations/{}", compId);
        return compilationService.getCompilationById(compId);
    }
}
package ru.practicum.explorewithme.server.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.compilation.dto.CompilationDto;
import ru.practicum.explorewithme.server.service.CompilationServiceImpl;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
@Validated
public class PublicCompilationController {
    private final CompilationServiceImpl compilationServiceImpl;

    // GET /compilations
    // Получение подборок событий с возможностью фильтрации по закреплению
    @GetMapping
    public List<CompilationDto> getAll(@RequestParam(required = false) Boolean pinned,
                                       @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                       @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("[PublicCompilationController] GET /compilations?pinned={}&from={}&size={}",
                pinned, from, size);

        List<CompilationDto> compilations = compilationServiceImpl.getAll(pinned, from, size);
        log.debug("[PublicCompilationController] Найдено {} подборок", compilations.size());

        return compilations;
    }

    // GET /compilations/{compId}
    // Получение подборки событий по её идентификатору
    @GetMapping("/{compId}")
    public CompilationDto getById(@PathVariable @Positive Long compId) {
        log.info("[PublicCompilationController] GET /compilations/{}", compId);

        CompilationDto compilation = compilationServiceImpl.getById(compId);
        log.debug("[PublicCompilationController] Подборка найдена: id={}, title='{}', events={}",
                compId, compilation.getTitle(), compilation.getEvents().size());

        return compilation;
    }
}
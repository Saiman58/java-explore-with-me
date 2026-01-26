package ru.practicum.explorewithme.server.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.compilation.dto.CompilationDto;
import ru.practicum.explorewithme.compilation.dto.NewCompilationDto;
import ru.practicum.explorewithme.compilation.dto.UpdateCompilationRequest;
import ru.practicum.explorewithme.server.service.CompilationServiceImpl;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
@Validated
public class AdminCompilationController {
    private final CompilationServiceImpl compilationServiceImpl;

    // POST /admin/compilations
    // Добавление новой подборки
    @PostMapping
    public ResponseEntity<CompilationDto> create(@Valid @RequestBody NewCompilationDto newCompilation) {
        log.info("[AdminCompilationController] POST /admin/compilations: title='{}', events={}",
                newCompilation.getTitle(), newCompilation.getEvents());

        CompilationDto created = compilationServiceImpl.create(newCompilation);
        log.info("[AdminCompilationController] Подборка создана: id={}, title={}",
                created.getId(), created.getTitle());

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // GET /admin/compilations
    // Получение подборок событий
    @GetMapping
    public ResponseEntity<List<CompilationDto>> getAll(@RequestParam(required = false) Boolean pinned,
                                                       @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                       @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("[AdminCompilationController] GET /admin/compilations?pinned={}&from={}&size={}",
                pinned, from, size);

        List<CompilationDto> compilations = compilationServiceImpl.getAll(pinned, from, size);
        log.debug("[AdminCompilationController] Найдено {} подборок", compilations.size());

        return ResponseEntity.ok(compilations);
    }

    // DELETE /admin/compilations/{compId}
    // Удаление подборки
    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long compId) {
        log.info("[AdminCompilationController] DELETE /admin/compilations/{}", compId);

        compilationServiceImpl.delete(compId);
        log.info("[AdminCompilationController] Подборка удалена: id={}", compId);
    }

    // PATCH /admin/compilations/{compId}
    // Обновление информации о подборке
    @PatchMapping("/{compId}")
    public CompilationDto update(@PathVariable @Positive Long compId,
                                 @Valid @RequestBody UpdateCompilationRequest update) {

        log.info("[AdminCompilationController] PATCH /admin/compilations/{}", compId);
        log.debug("[AdminCompilationController] Обновление подборки: title={}, pinned={}, events={}",
                update.getTitle(), update.getPinned(), update.getEvents());

        CompilationDto updated = compilationServiceImpl.update(compId, update);
        log.info("[AdminCompilationController] Подборка обновлена: id={}, title={}",
                compId, updated.getTitle());

        return updated;
    }
}
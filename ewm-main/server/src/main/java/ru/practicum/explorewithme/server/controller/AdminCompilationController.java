package ru.practicum.explorewithme.server.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.server.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.server.dto.compilation.NewCompilationDto;
import ru.practicum.explorewithme.server.dto.compilation.UpdateCompilationRequest;
import ru.practicum.explorewithme.server.service.CompilationService;

@Slf4j
@Validated
@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
public class AdminCompilationController {

    private final CompilationService compilationService;

    // POST /admin/compilations
    // Добавление новой подборки событий
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(@Valid @RequestBody NewCompilationDto newCompilationDto) {
        log.info("POST /admin/compilations - название: '{}', закреплена: {}",
                newCompilationDto.getTitle(), newCompilationDto.getPinned());
        return compilationService.createCompilation(newCompilationDto);
    }

    // DELETE /admin/compilations/{compId}
    // Удаление подборки событий
    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable @Positive Long compId) {
        log.info("DELETE /admin/compilations/{}", compId);
        compilationService.deleteCompilation(compId);
    }

    // PATCH /admin/compilations/{compId}
    // Обновление информации о подборке событий
    @PatchMapping("/{compId}")
    public CompilationDto updateCompilation(
            @PathVariable @Positive Long compId,
            @Valid @RequestBody UpdateCompilationRequest updateRequest) {
        log.info("PATCH /admin/compilations/{}: {}", compId, updateRequest);
        return compilationService.updateCompilation(compId, updateRequest);
    }
}
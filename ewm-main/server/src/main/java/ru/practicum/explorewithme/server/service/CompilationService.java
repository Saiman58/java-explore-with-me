package ru.practicum.explorewithme.server.service;

import ru.practicum.explorewithme.compilation.dto.CompilationDto;
import ru.practicum.explorewithme.compilation.dto.NewCompilationDto;
import ru.practicum.explorewithme.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    // Создать подборку
    CompilationDto create(NewCompilationDto newCompilation);

    // Обновить подборку
    CompilationDto update(Long compId, UpdateCompilationRequest update);

    // Удалить подборку
    void delete(Long compId);

    // Получить все подборки
    List<CompilationDto> getAll(Boolean pinned, Integer from, Integer size);

    // Получить подборку по ID
    CompilationDto getById(Long compId);
}
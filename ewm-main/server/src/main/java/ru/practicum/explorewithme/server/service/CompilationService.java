package ru.practicum.explorewithme.server.service;

import ru.practicum.explorewithme.server.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.server.dto.compilation.NewCompilationDto;
import ru.practicum.explorewithme.server.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    //Создать подборку (для администратора)
    CompilationDto createCompilation(NewCompilationDto newCompilationDto);

    // Удалить подборку (для администратора)
    void deleteCompilation(Long compId);

    //Обновить подборку (для администратора)
    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest);

    //Получить список подборок (публичный)
    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

    // Получить подборку по ID (публичный)
    CompilationDto getCompilationById(Long compId);
}

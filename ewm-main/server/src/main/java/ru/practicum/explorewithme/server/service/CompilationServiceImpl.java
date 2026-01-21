package ru.practicum.explorewithme.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.server.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.server.dto.compilation.NewCompilationDto;
import ru.practicum.explorewithme.server.dto.compilation.UpdateCompilationRequest;
import ru.practicum.explorewithme.server.entity.Compilation;
import ru.practicum.explorewithme.server.entity.Event;
import ru.practicum.explorewithme.server.exception.ConflictException;
import ru.practicum.explorewithme.server.exception.NotFoundException;
import ru.practicum.explorewithme.server.mapper.CompilationMapper;
import ru.practicum.explorewithme.server.repository.CompilationRepository;
import ru.practicum.explorewithme.server.repository.EventRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        log.info("Создание новой подборки: {}", newCompilationDto.getTitle());

        // Проверка уникальности названия
        if (compilationRepository.existsByTitle(newCompilationDto.getTitle())) {
            throw new ConflictException("Подборка с названием '" +
                    newCompilationDto.getTitle() + "' уже существует");
        }

        // Создаем сущность из DTO
        Compilation compilation = compilationMapper.toEntity(newCompilationDto);

        // Загружаем события, если они указаны
        Set<Event> events = new HashSet<>();
        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            List<Event> foundEvents = eventRepository.findAllById(newCompilationDto.getEvents());

            // Проверяем, что все события найдены
            if (foundEvents.size() != newCompilationDto.getEvents().size()) {
                throw new NotFoundException("Некоторые события не найдены");
            }

            events.addAll(foundEvents);
        }

        compilation.setEvents(events);

        // Сохраняем в БД
        Compilation savedCompilation = compilationRepository.save(compilation);
        log.info("Подборка создана с id: {}", savedCompilation.getId());

        // Возвращаем DTO без статистики
        return compilationMapper.toDtoWithoutStats(savedCompilation);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        log.info("Удаление подборки с id: {}", compId);

        // Проверяем существование подборки
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Подборка с id=" + compId + " не найдена");
        }

        // Удаляем
        compilationRepository.deleteById(compId);
        log.info("Подборка с id={} удалена", compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        log.info("Обновление подборки с id: {}", compId);

        // Находим подборку
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id=" + compId + " не найдена"));

        // Валидация названия
        if (updateRequest.getTitle() != null && !updateRequest.getTitle().isBlank()) {
            String newTitle = updateRequest.getTitle().trim();

            if (!newTitle.equals(compilation.getTitle()) &&
                    compilationRepository.existsByTitle(newTitle)) {
                throw new ConflictException("Подборка с названием '" + newTitle + "' уже существует");
            }
            compilation.setTitle(newTitle);
        }

        // Обновление закрепления
        if (updateRequest.getPinned() != null) {
            compilation.setPinned(updateRequest.getPinned());
        }

        // Обновление событий с валидацией
        if (updateRequest.getEvents() != null) {
            if (updateRequest.getEvents().isEmpty()) {
                compilation.getEvents().clear();
            } else {
                Set<Long> eventIds = new HashSet<>(updateRequest.getEvents());
                List<Event> events = loadAndValidateEvents(eventIds);
                compilation.setEvents(new HashSet<>(events));
            }
        }

        // Сохранение
        Compilation updated = compilationRepository.save(compilation);
        log.info("Подборка с id={} обновлена", compId);

        return compilationMapper.toDtoWithoutStats(updated);
    }

    private List<Event> loadAndValidateEvents(Set<Long> eventIds) {
        List<Event> events = eventRepository.findAllById(eventIds);

        if (events.size() != eventIds.size()) {
            Set<Long> foundIds = events.stream()
                    .map(Event::getId)
                    .collect(Collectors.toSet());
            Set<Long> missingIds = eventIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toSet());

            throw new NotFoundException("События с id=" + missingIds + " не найдены");
        }

        return events;
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        log.info("Получение списка подборок: pinned={}, from={}, size={}",
                pinned, from, size);

        // Проверка размера страницы
        if (size <= 0) {
            throw new IllegalArgumentException("Размер страницы должен быть больше 0");
        }

        // Создаем объект пагинации
        Pageable pageable = PageRequest.of(from / size, size);

        // Получаем подборки с фильтром
        List<Compilation> compilations = compilationRepository
                .findAllByPinned(pinned, pageable)
                .getContent();

        // Преобразуем в DTO
        return compilations.stream()
                .map(compilationMapper::toDtoWithoutStats)
                .collect(Collectors.toList());
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        log.info("Получение подборки с id: {}", compId);

        // Находим подборку
        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Подборка с id=" + compId + " не найдена"));

        return compilationMapper.toDtoWithoutStats(compilation);
    }
}
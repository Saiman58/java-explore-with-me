package ru.practicum.explorewithme.server.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.compilation.dto.CompilationDto;
import ru.practicum.explorewithme.compilation.dto.NewCompilationDto;
import ru.practicum.explorewithme.compilation.dto.UpdateCompilationRequest;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.server.repository.CompilationRepository;
import ru.practicum.explorewithme.server.entity.Compilation;
import ru.practicum.explorewithme.server.entity.Event;
import ru.practicum.explorewithme.server.exception.EntityNotFoundException;
import ru.practicum.explorewithme.server.mapper.CompilationMapper;
import ru.practicum.explorewithme.server.mapper.EventMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventService eventService;  // Изменено с EventServiceImpl на интерфейс EventService
    private final CompilationMapper compilationMapper;
    private final EventMapper eventMapper;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String COMPILATION_NOT_FOUND = "Подборка с id=%d не найдена";
    private static final String COMPILATION_TITLE_EXISTS = "Название подборки уже существует: %s";
    private static final boolean DEFAULT_PINNED = false;

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto newCompilation) {
        log.info("[CompilationService] Создание подборки: {}", newCompilation.getTitle());

        if (compilationRepository.existsByTitle(newCompilation.getTitle())) {
            log.warn("[CompilationService] Подборка с названием '{}' уже существует", newCompilation.getTitle());
            throw new IllegalStateException(String.format(COMPILATION_TITLE_EXISTS, newCompilation.getTitle()));
        }

        Compilation compilation = Compilation.builder()
                .title(newCompilation.getTitle())
                .pinned(newCompilation.getPinned() != null ? newCompilation.getPinned() : DEFAULT_PINNED)
                .build();

        Set<Event> events = new HashSet<>();
        if (newCompilation.getEvents() != null && !newCompilation.getEvents().isEmpty()) {
            newCompilation.getEvents().forEach(eventId -> {
                try {
                    events.add(eventService.getById(eventId));  // Используем интерфейс
                } catch (Exception e) {
                    log.error("[CompilationService] Ошибка при получении события {}: {}", eventId, e.getMessage());
                    throw e;
                }
            });
        }
        compilation.setEvents(events);

        compilation = compilationRepository.save(compilation);
        entityManager.flush();

        log.info("[CompilationService] Подборка создана: id={}, title={}, events={}",
                compilation.getId(), compilation.getTitle(), events.size());

        List<EventShortDto> eventDtos = events.stream()
                .map(e -> eventMapper.toShortDto(e,
                        eventService.getConfirmedCount(e.getId()),
                        eventService.getViewsForEvent(e.getId())))
                .collect(Collectors.toList());

        return compilationMapper.toDto(compilation, eventDtos);
    }

    @Override
    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationRequest update) {
        log.info("[CompilationService] Обновление подборки: id={}", compId);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> {
                    log.error("[CompilationService] Подборка не найдена: id={}", compId);
                    return new EntityNotFoundException(String.format(COMPILATION_NOT_FOUND, compId));
                });

        if (update.getTitle() != null) {
            if (compilationRepository.existsByTitleAndIdNot(update.getTitle(), compId)) {
                log.warn("[CompilationService] Название подборки уже занято: {}", update.getTitle());
                throw new IllegalStateException(String.format(COMPILATION_TITLE_EXISTS, update.getTitle()));
            }
            compilation.setTitle(update.getTitle());
        }

        if (update.getPinned() != null) {
            compilation.setPinned(update.getPinned());
        }

        Set<Event> events = new HashSet<>();
        if (update.getEvents() != null && !update.getEvents().isEmpty()) {
            update.getEvents().forEach(eventId -> {
                try {
                    events.add(eventService.getById(eventId));  // Используем интерфейс
                } catch (Exception e) {
                    log.error("[CompilationService] Ошибка при получении события {}: {}", eventId, e.getMessage());
                    throw e;
                }
            });
        }
        compilation.setEvents(events);

        compilation = compilationRepository.save(compilation);

        log.info("[CompilationService] Подборка обновлена: id={}, events={}", compId, events.size());

        List<EventShortDto> eventDtos = events.stream()
                .map(e -> eventMapper.toShortDto(e,
                        eventService.getConfirmedCount(e.getId()),
                        eventService.getViewsForEvent(e.getId())))
                .collect(Collectors.toList());

        return compilationMapper.toDto(compilation, eventDtos);
    }

    @Override
    @Transactional
    public void delete(Long compId) {
        log.info("[CompilationService] Удаление подборки: id={}", compId);

        if (!compilationRepository.existsById(compId)) {
            log.error("[CompilationService] Подборка не найдена для удаления: id={}", compId);
            throw new EntityNotFoundException(String.format(COMPILATION_NOT_FOUND, compId));
        }

        compilationRepository.deleteById(compId);
        log.info("[CompilationService] Подборка удалена: id={}", compId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getAll(Boolean pinned, Integer from, Integer size) {
        log.debug("[CompilationService] Получение подборок: pinned={}, from={}, size={}", pinned, from, size);

        PageRequest pageable = PageRequest.of(from / size, size);
        List<Compilation> compilations;

        if (pinned != null) {
            Page<Compilation> page = compilationRepository.findAllByPinned(pinned, pageable);
            compilations = page.getContent();
        } else {
            Page<Compilation> page = compilationRepository.findAll(pageable);
            compilations = page.getContent();
        }

        log.debug("[CompilationService] Найдено {} подборок", compilations.size());

        return compilations.stream()
                .map(comp -> {
                    List<EventShortDto> eventDtos = comp.getEvents() != null ?
                            comp.getEvents().stream()
                                    .map(e -> eventMapper.toShortDto(e,
                                            eventService.getConfirmedCount(e.getId()),
                                            eventService.getViewsForEvent(e.getId())))
                                    .collect(Collectors.toList()) : List.of();
                    return compilationMapper.toDto(comp, eventDtos);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getById(Long compId) {
        log.debug("[CompilationService] Получение подборки: id={}", compId);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> {
                    log.error("[CompilationService] Подборка не найдена: id={}", compId);
                    return new EntityNotFoundException(String.format(COMPILATION_NOT_FOUND, compId));
                });

        List<EventShortDto> eventDtos = compilation.getEvents() != null ?
                compilation.getEvents().stream()
                        .map(e -> eventMapper.toShortDto(e,
                                eventService.getConfirmedCount(e.getId()),
                                eventService.getViewsForEvent(e.getId())))
                        .collect(Collectors.toList()) : List.of();

        log.debug("[CompilationService] Подборка найдена: id={}, events={}", compId, eventDtos.size());
        return compilationMapper.toDto(compilation, eventDtos);
    }
}
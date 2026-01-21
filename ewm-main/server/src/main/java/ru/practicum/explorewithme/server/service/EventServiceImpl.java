package ru.practicum.explorewithme.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.server.dto.event.EventFullDto;
import ru.practicum.explorewithme.server.dto.event.EventShortDto;
import ru.practicum.explorewithme.server.dto.event.NewEventDto;
import ru.practicum.explorewithme.server.dto.event.UpdateEventAdminRequest;
import ru.practicum.explorewithme.server.dto.event.UpdateEventUserRequest;
import ru.practicum.explorewithme.server.entity.*;
import ru.practicum.explorewithme.server.exception.ConflictException;
import ru.practicum.explorewithme.server.exception.NotFoundException;
import ru.practicum.explorewithme.server.exception.ValidationException;
import ru.practicum.explorewithme.server.mapper.EventMapper;
import ru.practicum.explorewithme.server.repository.CategoryRepository;
import ru.practicum.explorewithme.server.repository.EventRepository;
import ru.practicum.explorewithme.server.repository.RequestRepository;
import ru.practicum.explorewithme.server.repository.UserRepository;
import ru.practicum.explorewithme.server.service.client.StatClient;
import ru.practicum.explorewithme.stats.dto.EndpointHit;
import ru.practicum.explorewithme.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final RequestRepository requestRepository;
    private final StatClient statClient;

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        log.info("Создание события пользователем с id={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория с id=" + newEventDto.getCategory() + " не найдена"));

        validateEventDate(newEventDto.getEventDate(), 2, "создания события");

        Event event = eventMapper.toEntity(newEventDto);
        event.setInitiator(user);
        event.setCategory(category);
        Event savedEvent = eventRepository.save(event);

        log.info("Событие создано с id={}", savedEvent.getId());

        return eventMapper.eventFullDto(savedEvent, 0L, 0L);
    }

    @Override
    public List<EventFullDto> getUserEvents(Long userId, int from, int size) {
        log.info("Получение событий пользователя с id={}, from={}, size={}", userId, from, size);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findByInitiatorId(userId, pageable).getContent();

        return events.stream()
                .map(event -> eventMapper.eventFullDto(
                        event,
                        getConfirmedRequests(event.getId()),
                        getViews(event.getId())
                ))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        log.info("Получение события с id={} пользователя с id={}", eventId, userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Событие с id=" + eventId + " не найдено для пользователя с id=" + userId));

        return eventMapper.eventFullDto(
                event,
                getConfirmedRequests(eventId),
                getViews(eventId)
        );
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest) {
        log.info("Обновление события с id={} пользователем с id={}", eventId, userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException(
                        "Событие с id=" + eventId + " не найдено для пользователя с id=" + userId));

        if (event.getState() != EventState.PENDING && event.getState() != EventState.CANCELED) {
            throw new ConflictException("Изменить можно только отмененные события или события в состоянии ожидания модерации");
        }

        if (updateRequest.getEventDate() != null) {
            validateEventDate(updateRequest.getEventDate(), 2, "обновления события");
        }

        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id=" + updateRequest.getCategory() + " не найдена"));
            event.setCategory(category);
        }

        eventMapper.updateFromUserRequest(event, updateRequest);

        Event updatedEvent = eventRepository.save(event);
        log.info("Событие с id={} обновлено пользователем", eventId);

        return eventMapper.eventFullDto(
                updatedEvent,
                getConfirmedRequests(eventId),
                getViews(eventId)
        );
    }

    @Override
    public List<EventShortDto> getPublishedEvents(String text, List<Long> categories, Boolean paid,
                                                  LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                  Boolean onlyAvailable, String sort,
                                                  int from, int size) {
        log.info("Поиск опубликованных событий с фильтрами");

        if (categories != null && categories.isEmpty()) {
            categories = null;
        }

        if (text != null) {
            text = text.trim();
            if (text.isEmpty()) {
                text = null;
            } else if (text.length() > 7000) {
                throw new ValidationException("Длина текста для поиска не может превышать 7000 символов");
            }
        }

        validateSearchParameters(rangeStart, rangeEnd, sort);

        if (rangeStart == null && rangeEnd == null) {
            rangeStart = LocalDateTime.now();
            log.info("Установлена текущая дата как rangeStart: {}", rangeStart);
        }

        Pageable pageable = PageRequest.of(from / size, size);

        Page<Event> eventsPage = eventRepository.findPublishedEventsWithFilters(
                text, categories, paid, rangeStart, rangeEnd, pageable
        );

        List<Event> events = new ArrayList<>(eventsPage.getContent());
        log.info("Найдено {} событий после базовых фильтров", events.size());

        if (Boolean.TRUE.equals(onlyAvailable)) {
            events = events.stream()
                    .filter(event -> {
                        Long confirmed = getConfirmedRequests(event.getId());
                        return event.getParticipantLimit() == 0 ||
                                confirmed < event.getParticipantLimit();
                    })
                    .collect(Collectors.toList());
            log.info("После фильтра onlyAvailable осталось {} событий", events.size());
        }

        if ("VIEWS".equals(sort)) {
            // TODO: Реализовать сортировку по просмотрам
            events.sort(Comparator.comparing(Event::getEventDate));
            log.info("Применена сортировка по VIEWS (пока реализовано как EVENT_DATE)");
        } else if ("EVENT_DATE".equals(sort)) {
            events.sort(Comparator.comparing(Event::getEventDate));
            log.info("Применена сортировка по EVENT_DATE");
        }

        if (Boolean.TRUE.equals(onlyAvailable) && from > 0) {
            int start = Math.min(from, events.size());
            int end = Math.min(start + size, events.size());
            events = events.subList(start, end);
            log.info("Применена ручная пагинация: start={}, end={}", start, end);
        }

        log.info("Возвращаем {} событий", events.size());
        return events.stream()
                .map(event -> eventMapper.toShortDto(
                        event,
                        getConfirmedRequests(event.getId()),
                        getViews(event.getId())
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<EventFullDto> getEventsByAdmin(List<Long> users, List<String> states,
                                               List<Long> categories, LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd, int from, int size) {
        log.info("Поиск событий администратором");

        validateSearchParameters(rangeStart, rangeEnd, null);

        if (users != null && users.isEmpty()) {
            users = null;
        }
        if (categories != null && categories.isEmpty()) {
            categories = null;
        }

        List<EventState> eventStates = null;
        if (states != null && !states.isEmpty()) {
            try {
                eventStates = states.stream()
                        .map(String::toUpperCase)
                        .map(EventState::valueOf)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Недопустимое значение состояния: " + e.getMessage());
            }
        } else if (states != null && states.isEmpty()) {
            states = null;
        }

        Pageable pageable = PageRequest.of(from / size, size);

        log.debug("Вызов репозитория с параметрами: users={}, states={}, categories={}, rangeStart={}, rangeEnd={}",
                users, eventStates, categories, rangeStart, rangeEnd);

        Page<Event> eventsPage = eventRepository.findEventsByAdminFilters(
                users, eventStates, categories, rangeStart, rangeEnd, pageable
        );

        List<Event> events = eventsPage.getContent();
        log.info("Найдено {} событий для администратора", events.size());

        return events.stream()
                .map(event -> eventMapper.eventFullDto(
                        event,
                        getConfirmedRequests(event.getId()),
                        getViews(event.getId())
                ))
                .collect(Collectors.toList());
    }

    @Override
    public EventFullDto getPublishedEvent(Long eventId, String remoteAddr) {
        log.info("Получение события {} от IP: {}", eventId, remoteAddr);

        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Опубликованное событие с id=" + eventId + " не найдено"));

        saveEventView(eventId, remoteAddr);

        Long views = getViewsForEvent(eventId);
        views = (views != null ? views : 0L) + 1;

        log.info("Просмотры: {}", views);

        Long confirmedRequests = getConfirmedRequests(eventId);
        return eventMapper.eventFullDto(event, confirmedRequests, views);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest) {
        log.info("Обновление события с id={} администратором", eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        if (updateRequest.getStateAction() == UpdateEventAdminRequest.StateAction.PUBLISH_EVENT) {
            if (event.getState() != EventState.PENDING) {
                throw new ConflictException("Нельзя опубликовать событие, потому что оно находится в неподходящем состоянии: " + event.getState());
            }
            if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ConflictException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации");
            }
        }

        if (updateRequest.getStateAction() == UpdateEventAdminRequest.StateAction.REJECT_EVENT) {
            if (event.getState() == EventState.PUBLISHED) {
                throw new ConflictException("Нельзя отклонить уже опубликованное событие");
            }
            if (event.getState() == EventState.CANCELED) {
                throw new ConflictException("Событие уже отменено");
            }
        }

        if (updateRequest.getEventDate() != null) {
            validateEventDate(updateRequest.getEventDate(), 1, "публикации события");
        }

        if (updateRequest.getCategory() != null) {
            Category category = categoryRepository.findById(updateRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория с id=" + updateRequest.getCategory() + " не найдена"));
            event.setCategory(category);
        }

        eventMapper.updateFromAdminRequest(event, updateRequest);

        Event updatedEvent = eventRepository.save(event);
        log.info("Событие с id={} обновлено администратором", eventId);

        return eventMapper.eventFullDto(
                updatedEvent,
                getConfirmedRequests(eventId),
                getViews(eventId)
        );
    }

    @Override
    public Long getViewsForEvent(Long eventId) {
        List<ViewStats> stats = statClient.getStats(LocalDateTime.now().minusYears(1),
                LocalDateTime.now(), List.of("/events/" + eventId), false).getBody();
        return stats != null && !stats.isEmpty() ? stats.get(0).getHits() : 0L;
    }

    public Long getViewsForEvent(Long eventId, boolean unique) {
        log.info("Получение просмотров для события {} (unique={})", eventId, unique);

        try {
            String uri = "/events/" + eventId;
            LocalDateTime start = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
            LocalDateTime end = LocalDateTime.of(2030, 12, 31, 23, 59, 59);

            List<ViewStats> stats = statClient.getStats(start, end, List.of(uri), unique).getBody();

            if (stats != null && !stats.isEmpty()) {
                return stats.get(0).getHits();
            }

            return 0L;

        } catch (Exception e) {
            log.error("Ошибка получения статистики: {}", e.getMessage());
            return 0L;
        }
    }

    @Override
    public Long getConfirmedRequests(Long eventId) {
        return requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    //ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===

    private void saveEventView(Long eventId, String remoteAddr) {
        log.info("Сохранение статистики для события: {}", eventId);

        try {
            String uri = "/events/" + eventId;

            EndpointHit hit = EndpointHit.builder()
                    .app("ewm-main")
                    .uri(uri)
                    .ip(remoteAddr)
                    .timestamp(LocalDateTime.now())
                    .build();

            statClient.postHit(hit);
            log.info("Статистика сохранена");

        } catch (Exception e) {
            log.error("Ошибка сохранения статистики: {}", e.getMessage());
        }
    }

    private Long getViews(Long eventId) {
        return getViewsForEvent(eventId);
    }

    private void validateEventDate(LocalDateTime eventDate, int hours, String operation) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(hours))) {
            throw new ValidationException(
                    String.format("Дата события должна быть не ранее чем через %d часа от момента %s",
                            hours, operation)
            );
        }
    }

    private void validateSearchParameters(LocalDateTime rangeStart, LocalDateTime rangeEnd, String sort) {
        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new ValidationException(
                    String.format("Дата окончания rangeEnd (%s) не может быть раньше даты начала rangeStart (%s)",
                            rangeEnd, rangeStart)
            );
        }

        if (sort != null) {
            if (!"EVENT_DATE".equals(sort) && !"VIEWS".equals(sort)) {
                throw new ValidationException(
                        String.format("Недопустимое значение параметра sort: '%s'. Допустимые значения: EVENT_DATE, VIEWS", sort)
                );
            }
        }
    }
}
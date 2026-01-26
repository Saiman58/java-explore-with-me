package ru.practicum.explorewithme.server.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.event.dto.*;
import ru.practicum.explorewithme.server.entity.*;
import ru.practicum.explorewithme.server.exception.EntityNotFoundException;
import ru.practicum.explorewithme.server.mapper.EventMapper;
import ru.practicum.explorewithme.server.repository.EventRepository;
import ru.practicum.explorewithme.server.repository.RequestRepository;
import ru.practicum.explorewithme.client.StatClient;
import ru.practicum.explorewithme.stats.dto.EndpointHit;
import ru.practicum.explorewithme.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final RequestRepository requestRepository;
    private final StatClient statClient;
    private final EventMapper eventMapper;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String APP_NAME = "ewm-main";
    private static final String EVENTS_URI = "/events";
    private static final EventState PENDING_STATE = EventState.PENDING;
    private static final long USER_HOURS_AHEAD = 2L;
    private static final long ADMIN_HOURS_AHEAD = 1L;

    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto newEvent) {
        log.info("[EventService] Создание события: userId={}, title={}", userId, newEvent.getTitle());

        User user = userService.getById(userId);

        if (newEvent.getEventDate().isBefore(LocalDateTime.now().plusHours(USER_HOURS_AHEAD))) {
            log.warn("[EventService] Дата события слишком рано: {}", newEvent.getEventDate());
            throw new IllegalArgumentException("Дата события должна быть не менее чем через 2 часа");
        }

        Category category = categoryService.getEntityById(newEvent.getCategory());
        EventLocation locationEntity = convertToEntity(newEvent.getLocation());

        Event event = Event.builder()
                .annotation(newEvent.getAnnotation())
                .description(newEvent.getDescription())
                .eventDate(newEvent.getEventDate())
                .initiator(user)
                .location(locationEntity)
                .paid(newEvent.getPaid() != null ? newEvent.getPaid() : false)
                .participantLimit(newEvent.getParticipantLimit() != null ? newEvent.getParticipantLimit() : 0)
                .requestModeration(newEvent.getRequestModeration() != null ? newEvent.getRequestModeration() : true)
                .state(PENDING_STATE)
                .createdOn(LocalDateTime.now())
                .category(category)
                .title(newEvent.getTitle())
                .build();

        event = eventRepository.save(event);
        entityManager.flush();

        log.info("[EventService] Событие создано: id={}, userId={}", event.getId(), userId);

        Long confirmedRequests = getConfirmedCount(event.getId());
        Long views = getViewsForEvent(event.getId());

        return eventMapper.toFullDto(event, confirmedRequests, views, true);
    }

    @Override
    @Transactional
    public EventFullDto updateUser(Long userId, Long eventId, UpdateEventUserRequest update) {
        log.info("[EventService] Обновление события пользователем: userId={}, eventId={}", userId, eventId);

        Event event = getById(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            log.error("[EventService] Доступ запрещён: userId={}, initiatorId={}", userId,
                    event.getInitiator().getId());
            throw new EntityNotFoundException("Событие недоступно для этого пользователя");
        }

        if (event.getState() == EventState.PUBLISHED) {
            log.warn("[EventService] Нельзя обновить опубликованное событие: eventId={}", eventId);
            throw new IllegalStateException("Опубликованные события нельзя обновлять");
        }

        if (update.getEventDate() != null && update.getEventDate().isBefore(LocalDateTime.now()
                .plusHours(USER_HOURS_AHEAD))) {
            log.warn("[EventService] Дата события слишком рано: {}", update.getEventDate());
            throw new IllegalArgumentException("Дата события должна быть не менее чем через 2 часа");
        }

        if (update.getStateAction() != null) {
            switch (update.getStateAction()) {
                case "SEND_TO_REVIEW":
                    if (event.getState() == EventState.CANCELED) {
                        event.setState(PENDING_STATE);
                        log.debug("[EventService] Событие отправлено на модерацию: eventId={}", eventId);
                    }
                    break;
                case "CANCEL_REVIEW":
                    if (event.getState() == EventState.PENDING) {
                        event.setState(EventState.CANCELED);
                        log.debug("[EventService] Событие отменено: eventId={}", eventId);
                    }
                    break;
                default:
                    log.error("[EventService] Неверное действие: {}", update.getStateAction());
                    throw new IllegalStateException("Неверное действие для текущего состояния");
            }
        }

        updateFieldsUser(event, update);
        event = eventRepository.save(event);

        log.info("[EventService] Событие обновлено пользователем: eventId={}", eventId);

        Long confirmedRequests = getConfirmedCount(event.getId());
        Long views = getViewsForEvent(event.getId());

        return eventMapper.toFullDto(event, confirmedRequests, views, false);
    }

    @Override
    @Transactional
    public EventFullDto updateAdmin(Long eventId, UpdateEventAdminRequest update) {
        log.info("[EventService] Обновление события администратором: eventId={}", eventId);

        Event event = getById(eventId);

        if (update.getEventDate() != null &&
                update.getEventDate().isBefore(LocalDateTime.now().plusHours(ADMIN_HOURS_AHEAD))) {
            log.warn("[EventService] Дата события слишком рано для админа: {}", update.getEventDate());
            throw new IllegalArgumentException("Дата события должна быть не менее чем через 1 час");
        }

        if (update.getStateAction() != null) {
            switch (update.getStateAction()) {
                case "PUBLISH_EVENT":
                    if (event.getState() != EventState.PENDING) {
                        log.error("[EventService] Нельзя опубликовать: текущий state={}", event.getState());
                        throw new IllegalStateException("Невозможно опубликовать событие, текущее состояние: " +
                                event.getState());
                    }
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    log.debug("[EventService] Событие опубликовано: eventId={}", eventId);
                    break;
                case "REJECT_EVENT":
                    if (event.getState() == EventState.PUBLISHED) {
                        log.error("[EventService] Нельзя отклонить опубликованное: eventId={}", eventId);
                        throw new IllegalStateException("Опубликованные события нельзя отклонять");
                    }
                    event.setState(EventState.CANCELED);
                    log.debug("[EventService] Событие отклонено: eventId={}", eventId);
                    break;
                default:
                    log.error("[EventService] Неверное действие админа: {}", update.getStateAction());
                    throw new IllegalStateException("Неверное действие stateAction: " + update.getStateAction());
            }
        }

        updateFieldsAdmin(event, update);
        event = eventRepository.save(event);

        log.info("[EventService] Событие обновлено админом: eventId={}", eventId);

        Long confirmedRequests = getConfirmedCount(event.getId());
        Long views = getViewsForEvent(event.getId());

        return eventMapper.toFullDto(event, confirmedRequests, views, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        log.debug("[EventService] Получение событий пользователя: userId={}, from={}, size={}", userId, from, size);

        int safeFrom = from != null ? from : 0;
        int safeSize = size != null ? size : 10;

        PageRequest pageable = PageRequest.of(0, safeFrom + safeSize,
                Sort.by("eventDate").descending());
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);

        int endIndex = Math.min(events.size(), safeFrom + safeSize);
        if (safeFrom >= endIndex) {
            log.debug("[EventService] События пользователя не найдены: userId={}", userId);
            return List.of();
        }

        List<Event> resultEvents = events.subList(safeFrom, endIndex);
        log.debug("[EventService] Найдено {} событий пользователя: userId={}", resultEvents.size(), userId);

        return resultEvents.stream()
                .map(e -> {
                    Long confirmedRequests = getConfirmedCount(e.getId());
                    Long views = getViewsForEvent(e.getId());
                    return eventMapper.toShortDto(e, confirmedRequests, views);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> getAdminEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                             LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                             Integer from, Integer size) {
        log.debug("[EventService] Поиск событий админом: users={}, states={}, categories={}, from={}, size={}",
                users, states, categories, from, size);

        if (rangeStart == null) {
            rangeStart = LocalDateTime.of(1900, 1, 1, 0, 0);
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(10);
        }

        Sort sortBy = Sort.by("eventDate").descending().and(Sort.by("id").ascending());
        PageRequest pageable = PageRequest.of(from / size, size, sortBy);

        List<Event> events = eventRepository.findAdminEvents(users, states, categories, rangeStart, rangeEnd, pageable);
        log.debug("[EventService] Найдено {} событий для админа", events.size());

        return events.stream()
                .map(e -> {
                    Long confirmedRequests = getConfirmedCount(e.getId());
                    Long views = getViewsForEvent(e.getId());
                    return eventMapper.toFullDto(e, confirmedRequests, views, false);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
                                               String sort, Integer from, Integer size, String remoteAddr) {
        log.debug("[EventService] Поиск публичных событий: text={}, categories={}, paid={}, from={}, size={}, sort={}",
                text, categories, paid, from, size, sort);

        int page = from != null ? from / (size != null ? size : 10) : 0;
        int pageSize = size != null ? size : 10;

        Sort sortBy = "VIEWS".equals(sort) ? Sort.by("eventDate").descending() :
                Sort.by("eventDate").descending();
        PageRequest pageable = PageRequest.of(page, pageSize, sortBy);

        LocalDateTime start = rangeStart != null ? rangeStart : LocalDateTime.now();
        LocalDateTime end = rangeEnd != null ? rangeEnd : LocalDateTime.now().plusYears(1);

        if (rangeStart != null && rangeEnd != null && start.isAfter(end)) {
            log.warn("[EventService] Неверный диапазон дат: start={}, end={}", start, end);
            throw new IllegalArgumentException("Неверный диапазон дат: rangeStart должен быть раньше rangeEnd");
        }

        String searchText = (text == null || text.trim().isEmpty()) ? "" : text.trim();

        Page<Event> eventsPage = eventRepository.findPublicEvents(
                searchText,
                categories,
                paid,
                start,
                end,
                onlyAvailable,
                EventState.PUBLISHED,
                pageable
        );

        if (eventsPage.isEmpty()) {
            log.debug("[EventService] Публичные события не найдены");
            sendHit(remoteAddr);
            return List.of();
        }

        log.debug("[EventService] Найдено {} публичных событий", eventsPage.getContent().size());

        List<EventShortDto> shortDtos = eventsPage.getContent().stream()
                .map(e -> {
                    Long confirmedRequests = getConfirmedCount(e.getId());
                    Long views = getViewsForEvent(e.getId());
                    return eventMapper.toShortDto(e, confirmedRequests, views);
                })
                .collect(Collectors.toList());

        sendHit(remoteAddr);
        return shortDtos;
    }

    private void sendHit(String remoteAddr) {
        try {
            EndpointHit hit = EndpointHit.builder()
                    .app(APP_NAME)
                    .uri(EVENTS_URI)
                    .ip(remoteAddr)
                    .timestamp(LocalDateTime.now())
                    .build();
            statClient.postHit(hit);
            log.debug("[EventService] Статистика отправлена: ip={}", remoteAddr);
        } catch (Exception e) {
            log.warn("[EventService] Ошибка отправки статистики: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getPublicEvent(Long eventId, String remoteAddr) {
        log.debug("[EventService] Получение публичного события: eventId={}, ip={}", eventId, remoteAddr);

        Event event = getById(eventId);

        if (event.getState() != EventState.PUBLISHED) {
            log.error("[EventService] Событие не опубликовано: eventId={}, state={}", eventId, event.getState());
            throw new EntityNotFoundException("Событие не опубликовано");
        }

        try {
            EndpointHit hit = EndpointHit.builder()
                    .app(APP_NAME)
                    .uri(EVENTS_URI + "/" + eventId)
                    .ip(remoteAddr)
                    .timestamp(LocalDateTime.now())
                    .build();
            statClient.postHit(hit);
            log.debug("[EventService] Статистика отправлена для события: eventId={}", eventId);
        } catch (Exception e) {
            log.warn("[EventService] Ошибка отправки статистики для события {}: {}", eventId, e.getMessage());
        }

        Long views = getViewsForEvent(eventId);
        views = (views != null ? views : 0L) + 1;

        Long confirmedRequests = getConfirmedCount(event.getId());

        EventFullDto dto = eventMapper.toFullDto(event, confirmedRequests, views, false);
        dto.setViews(views);

        log.info("[EventService] Событие просмотрено: eventId={}, ip={}, views={}", eventId, remoteAddr, dto.getViews());

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        log.debug("[EventService] Получение события пользователя: userId={}, eventId={}", userId, eventId);

        Event event = getById(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            log.error("[EventService] Доступ запрещён: userId={}, initiatorId={}", userId, event.getInitiator().getId());
            throw new EntityNotFoundException("Событие недоступно для этого пользователя");
        }

        Long confirmedRequests = getConfirmedCount(event.getId());
        Long views = getViewsForEvent(event.getId());

        log.debug("[EventService] Событие пользователя найдено: eventId={}, userId={}", eventId, userId);
        return eventMapper.toFullDto(event, confirmedRequests, views, false);
    }

    @Override
    @Transactional(readOnly = true)
    public Event getById(Long eventId) {
        log.debug("[EventService] Получение события по ID: {}", eventId);

        return eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("[EventService] Событие не найдено: id={}", eventId);
                    return new EntityNotFoundException("Событие c id=" + eventId + " не найдено");
                });
    }

    private void updateFieldsUser(Event event, UpdateEventUserRequest update) {
        log.debug("[EventService] Обновление полей пользователем: eventId={}", event.getId());

        if (update.getAnnotation() != null) event.setAnnotation(update.getAnnotation());
        if (update.getCategory() != null) event.setCategory(categoryService.getEntityById(update.getCategory()));
        if (update.getDescription() != null) event.setDescription(update.getDescription());
        if (update.getEventDate() != null) event.setEventDate(update.getEventDate());
        if (update.getLocation() != null) event.setLocation(convertToEntity(update.getLocation()));
        if (update.getPaid() != null) event.setPaid(update.getPaid());
        if (update.getParticipantLimit() != null) event.setParticipantLimit(update.getParticipantLimit());
        if (update.getRequestModeration() != null) event.setRequestModeration(update.getRequestModeration());
        if (update.getTitle() != null) event.setTitle(update.getTitle());
    }

    private void updateFieldsAdmin(Event event, UpdateEventAdminRequest update) {
        log.debug("[EventService] Обновление полей администратором: eventId={}", event.getId());

        if (update.getAnnotation() != null) event.setAnnotation(update.getAnnotation());
        if (update.getCategory() != null) event.setCategory(categoryService.getEntityById(update.getCategory()));
        if (update.getDescription() != null) event.setDescription(update.getDescription());
        if (update.getEventDate() != null) event.setEventDate(update.getEventDate());
        if (update.getLocation() != null) event.setLocation(convertToEntity(update.getLocation()));
        if (update.getPaid() != null) event.setPaid(update.getPaid());
        if (update.getParticipantLimit() != null) event.setParticipantLimit(update.getParticipantLimit());
        if (update.getRequestModeration() != null) event.setRequestModeration(update.getRequestModeration());
        if (update.getTitle() != null) event.setTitle(update.getTitle());
    }

    private EventLocation convertToEntity(Location dto) {
        if (dto == null) return null;
        return new EventLocation(dto.getLat(), dto.getLon());
    }

    @Override
    @Transactional(readOnly = true)
    public Long getConfirmedCount(Long eventId) {
        Long count = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        log.debug("[EventService] Подтверждённых запросов: eventId={}, count={}", eventId, count);
        return count != null ? count : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getViewsForEvent(Long eventId) {
        try {
            List<ViewStats> stats = statClient.getStats(LocalDateTime.now().minusYears(1),
                    LocalDateTime.now(), List.of("/events/" + eventId), false).getBody();
            Long views = stats != null && !stats.isEmpty() ? stats.get(0).getHits() : 0L;
            log.debug("[EventService] Просмотров события: eventId={}, views={}", eventId, views);
            return views;
        } catch (Exception e) {
            log.warn("[EventService] Ошибка получения статистики для события {}: {}", eventId, e.getMessage());
            return 0L;
        }
    }
}
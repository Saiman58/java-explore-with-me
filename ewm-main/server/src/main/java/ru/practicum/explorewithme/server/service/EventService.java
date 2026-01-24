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
import ru.practicum.explorewithme.server.entity.RequestStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
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

    @Transactional
    public EventFullDto create(Long userId, NewEventDto newEvent) {
        log.info("Создание события для пользователя {} с заголовком '{}'", userId, newEvent.getTitle());
        User user = userService.getById(userId);

        if (newEvent.getEventDate().isBefore(LocalDateTime.now().plusHours(USER_HOURS_AHEAD))) {
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
        log.info("Событие создано с ID {}", event.getId());

        Long confirmedRequests = getConfirmedCount(event.getId());
        Long views = getViewsForEvent(event.getId());

        return eventMapper.toFullDto(event, confirmedRequests, views, true);
    }

    public EventFullDto updateUser(Long userId, Long eventId, UpdateEventUserRequest update) {
        log.info("Обновление события пользователя {} для пользователя {}", eventId, userId);
        Event event = getById(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new EntityNotFoundException("Событие недоступно для этого пользователя");
        }
        if (event.getState() == EventState.PUBLISHED) {
            throw new IllegalStateException("Опубликованные события нельзя обновлять");
        }
        if (update.getEventDate() != null && update.getEventDate().isBefore(LocalDateTime.now().plusHours(USER_HOURS_AHEAD))) {
            throw new IllegalArgumentException("Дата события должна быть не менее чем через 2 часа");
        }
        if (update.getStateAction() != null) {
            switch (update.getStateAction()) {
                case "SEND_TO_REVIEW":
                    if (event.getState() == EventState.CANCELED) event.setState(PENDING_STATE);
                    break;
                case "CANCEL_REVIEW":
                    if (event.getState() == EventState.PENDING) event.setState(EventState.CANCELED);
                    break;
                default:
                    throw new IllegalStateException("Неверное действие для текущего состояния");
            }
        }
        updateFieldsUser(event, update);
        event = eventRepository.save(event);
        log.info("Событие пользователя {} обновлено", eventId);

        Long confirmedRequests = getConfirmedCount(event.getId());
        Long views = getViewsForEvent(event.getId());

        return eventMapper.toFullDto(event, confirmedRequests, views, false);
    }

    @Transactional
    public EventFullDto updateAdmin(Long eventId, UpdateEventAdminRequest update) {
        log.info("Обновление события админом {}", eventId);
        Event event = getById(eventId);

        if (update.getEventDate() != null &&
                update.getEventDate().isBefore(LocalDateTime.now().plusHours(ADMIN_HOURS_AHEAD))) {
            throw new IllegalArgumentException("Дата события должна быть не менее чем через 1 час");
        }

        if (update.getStateAction() != null) {
            switch (update.getStateAction()) {
                case "PUBLISH_EVENT":
                    if (event.getState() != EventState.PENDING)
                        throw new IllegalStateException("Невозможно опубликовать событие, текущее состояние: " + event.getState());
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                case "REJECT_EVENT":
                    if (event.getState() == EventState.PUBLISHED)
                        throw new IllegalStateException("Опубликованные события нельзя отклонять");
                    event.setState(EventState.CANCELED);
                    break;
                default:
                    throw new IllegalStateException("Неверное действие stateAction: " + update.getStateAction());
            }
        }

        updateFieldsAdmin(event, update);
        event = eventRepository.save(event);
        log.info("Событие админа {} обновлено", eventId);

        Long confirmedRequests = getConfirmedCount(event.getId());
        Long views = getViewsForEvent(event.getId());

        return eventMapper.toFullDto(event, confirmedRequests, views, false);
    }

    public List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        int safeFrom = from != null ? from : 0;
        int safeSize = size != null ? size : 10;

        PageRequest pageable = PageRequest.of(0, safeFrom + safeSize, Sort.by("eventDate").descending());
        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);

        int endIndex = Math.min(events.size(), safeFrom + safeSize);
        if (safeFrom >= endIndex) {
            return List.of();
        }

        return events.subList(safeFrom, endIndex).stream()
                .map(e -> {
                    Long confirmedRequests = getConfirmedCount(e.getId());
                    Long views = getViewsForEvent(e.getId());
                    return eventMapper.toShortDto(e, confirmedRequests, views);
                })
                .collect(Collectors.toList());
    }

    public List<EventFullDto> getAdminEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                             LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        if (rangeStart == null) {
            rangeStart = LocalDateTime.of(1900, 1, 1, 0, 0);
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(10);
        }

        Sort sortBy = Sort.by("eventDate").descending().and(Sort.by("id").ascending());
        PageRequest pageable = PageRequest.of(from / size, size, sortBy);

        List<Event> events = eventRepository.findAdminEvents(users, states, categories, rangeStart, rangeEnd, pageable);
        return events.stream()
                .map(e -> {
                    Long confirmedRequests = getConfirmedCount(e.getId());
                    Long views = getViewsForEvent(e.getId());
                    return eventMapper.toFullDto(e, confirmedRequests, views, false);
                })
                .collect(Collectors.toList());
    }

    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
                                               String sort, Integer from, Integer size, String remoteAddr) {
        int page = from != null ? from / (size != null ? size : 10) : 0;
        int pageSize = size != null ? size : 10;

        Sort sortBy = "VIEWS".equals(sort) ? Sort.by("eventDate").descending() : Sort.by("eventDate").descending();

        PageRequest pageable = PageRequest.of(page, pageSize, sortBy);

        LocalDateTime start = rangeStart != null ? rangeStart : LocalDateTime.now();
        LocalDateTime end = rangeEnd != null ? rangeEnd : LocalDateTime.now().plusYears(1);

        if (rangeStart != null && rangeEnd != null && start.isAfter(end)) {
            log.warn("Неверный диапазон дат: rangeStart после rangeEnd");
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
            sendHit(remoteAddr);
            return List.of();
        }

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
        EndpointHit hit = EndpointHit.builder()
                .app(APP_NAME)
                .uri(EVENTS_URI)
                .ip(remoteAddr)
                .timestamp(LocalDateTime.now())
                .build();
        statClient.postHit(hit);
    }

    public EventFullDto getPublicEvent(Long eventId, String remoteAddr) {
        Event event = getById(eventId);

        if (event.getState() != EventState.PUBLISHED) {
            throw new EntityNotFoundException("Событие не опубликовано");
        }

        EndpointHit hit = EndpointHit.builder()
                .app(APP_NAME)
                .uri(EVENTS_URI + "/" + eventId)
                .ip(remoteAddr)
                .timestamp(LocalDateTime.now())
                .build();
        statClient.postHit(hit);

        Long views = getViewsForEvent(eventId);
        views = (views != null ? views : 0L) + 1;

        Long confirmedRequests = getConfirmedCount(event.getId());

        EventFullDto dto = eventMapper.toFullDto(event, confirmedRequests, views, false);
        dto.setViews(views);

        log.info("Просмотр события {} после запроса {}: {}", eventId, remoteAddr, dto.getViews());

        return dto;
    }

    public EventFullDto getUserEvent(Long userId, Long eventId) {
        Event event = getById(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new EntityNotFoundException("Событие недоступно для этого пользователя");
        }

        Long confirmedRequests = getConfirmedCount(event.getId());
        Long views = getViewsForEvent(event.getId());

        return eventMapper.toFullDto(event, confirmedRequests, views, false);
    }

    @Transactional(readOnly = true)
    public Event getById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Событие c id=" + eventId + " не найдено"));
    }

    private void updateFieldsUser(Event event, UpdateEventUserRequest update) {
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

    public Long getConfirmedCount(Long eventId) {
        return requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
    }

    public Long getViewsForEvent(Long eventId) {
        List<ViewStats> stats = statClient.getStats(LocalDateTime.now().minusYears(1),
                LocalDateTime.now(), List.of("/events/" + eventId), false).getBody();
        return stats != null && !stats.isEmpty() ? stats.get(0).getHits() : 0L;
    }
}
package ru.practicum.explorewithme.server.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.event.dto.NewEventDto;
import ru.practicum.explorewithme.event.dto.UpdateEventUserRequest;
import ru.practicum.explorewithme.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.request.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.server.service.EventServiceImpl;
import ru.practicum.explorewithme.server.service.RequestServiceImpl;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Validated
public class PrivateEventController {
    private final EventServiceImpl eventServiceImpl;
    private final RequestServiceImpl requestServiceImpl;

    // GET /users/{userId}/events
    // Получение событий, добавленных текущим пользователем
    @GetMapping
    public List<EventShortDto> getAll(@PathVariable @Positive Long userId,
                                      @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                      @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("[PrivateEventController] GET /users/{}/events?from={}&size={}", userId, from, size);

        List<EventShortDto> events = eventServiceImpl.getUserEvents(userId, from, size);
        log.debug("[PrivateEventController] Найдено {} событий пользователя {}", events.size(), userId);

        return events;
    }

    // POST /users/{userId}/events
    // Добавление нового события
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable @Positive Long userId,
                               @Valid @RequestBody NewEventDto newEvent) {

        log.info("[PrivateEventController] POST /users/{}/events - создание события: {}",
                userId, newEvent.getTitle());
        log.debug("[PrivateEventController] Данные события: title={}, category={}, eventDate={}",
                newEvent.getTitle(), newEvent.getCategory(), newEvent.getEventDate());

        EventFullDto createdEvent = eventServiceImpl.create(userId, newEvent);
        log.info("[PrivateEventController] Событие создано: id={}, title={}",
                createdEvent.getId(), createdEvent.getTitle());

        return createdEvent;
    }

    // GET /users/{userId}/events/{eventId}
    // Получение полной информации о событии добавленном текущим пользователем
    @GetMapping("/{eventId}")
    public EventFullDto getById(@PathVariable @Positive Long userId,
                                @PathVariable @Positive Long eventId) {

        log.info("[PrivateEventController] GET /users/{}/events/{}", userId, eventId);

        EventFullDto event = eventServiceImpl.getUserEvent(userId, eventId);
        log.debug("[PrivateEventController] Событие найдено: id={}, title={}, state={}",
                eventId, event.getTitle(), event.getState());

        return event;
    }

    // PATCH /users/{userId}/events/{eventId}
    // Изменение события добавленного текущим пользователем
    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable @Positive Long userId,
                               @PathVariable @Positive Long eventId,
                               @Valid @RequestBody UpdateEventUserRequest update) {

        log.info("[PrivateEventController] PATCH /users/{}/events/{}", userId, eventId);
        log.debug("[PrivateEventController] Обновление события: stateAction={}, title={}",
                update.getStateAction(), update.getTitle());

        EventFullDto updatedEvent = eventServiceImpl.updateUser(userId, eventId, update);
        log.info("[PrivateEventController] Событие обновлено: id={}, newState={}",
                eventId, updatedEvent.getState());

        return updatedEvent;
    }

    // GET /users/{userId}/events/{eventId}/requests
    // Получение информации о запросах на участие в событии текущего пользователя
    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequests(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId
    ) {
        log.info("[PrivateEventController] GET /users/{}/events/{}/requests", userId, eventId);

        List<ParticipationRequestDto> requests = requestServiceImpl.getByEvent(userId, eventId);
        log.debug("[PrivateEventController] Найдено {} заявок для события {}", requests.size(), eventId);

        return requests;
    }

    // PATCH /users/{userId}/events/{eventId}/requests
    // Изменение статуса заявок на участие в событии текущего пользователя
    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult changeRequestStatus(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest update
    ) {
        log.info("[PrivateEventController] PATCH /users/{}/events/{}/requests", userId, eventId);
        log.debug("[PrivateEventController] Изменение статусов заявок: status={}, count={}",
                update.getStatus(), update.getRequestIds().size());

        EventRequestStatusUpdateResult result = requestServiceImpl.changeStatus(userId, eventId, update);
        log.info("[PrivateEventController] Статусы изменены: подтверждено={}, отклонено={}",
                result.getConfirmedRequests().size(), result.getRejectedRequests().size());

        return result;
    }
}
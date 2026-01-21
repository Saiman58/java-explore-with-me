package ru.practicum.explorewithme.server.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.server.dto.event.EventFullDto;
import ru.practicum.explorewithme.server.dto.event.NewEventDto;
import ru.practicum.explorewithme.server.dto.event.UpdateEventUserRequest;
import ru.practicum.explorewithme.server.service.EventService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/events")
@Validated
public class PrivateEventController {
    private final EventService eventService;


    // POST /users/{userId}/events
    //Добавление нового события
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(
            @PathVariable @Positive Long userId,
            @Valid @RequestBody NewEventDto newEventDto) {

        log.info("POST /users/{}/events - создание события: {}", userId, newEventDto.getTitle());
        EventFullDto createdEvent = eventService.createEvent(userId, newEventDto);
        log.info("Событие создано с id={}", createdEvent.getId());

        return createdEvent;
    }

    // GET /users/{userId}/events
    // Получение событий, добавленных текущим пользователем
    @GetMapping
    public List<EventFullDto> getUserEvents(
            @PathVariable @Positive Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("GET /users/{}/events - получение событий пользователя, from={}, size={}",
                userId, from, size);

        return eventService.getUserEvents(userId, from, size);
    }

    // GET /users/{userId}/events/{eventId}
    //Получение полной информации о событии добавленном текущим пользователем
    @GetMapping("/{eventId}")
    public EventFullDto getUserEvent(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId) {

        log.info("GET /users/{}/events/{} - получение конкретного события пользователя",
                userId, eventId);

        return eventService.getUserEvent(userId, eventId);
    }

    //PATCH /users/{userId}/events/{eventId}
    //Изменение события добавленного текущим пользователем

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByUser(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId,
            @Valid @RequestBody UpdateEventUserRequest updateRequest) {

        log.info("PATCH /users/{}/events/{} - обновление события пользователем",
                userId, eventId);

        return eventService.updateEventByUser(userId, eventId, updateRequest);
    }
}
/*
Событие должно содержать поля: id, title,
annotation, category, paid, eventDate,
initiator, views, confirmedRequests, description,
participantLimit, state, createdOn, publishedOn, location, requestModeration
 */
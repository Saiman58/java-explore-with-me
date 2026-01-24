package ru.practicum.explorewithme.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Validated
public class PrivateEventController {
    private final EventServiceImpl eventServiceImpl;
    private final RequestServiceImpl requestServiceImpl;

    @GetMapping
    public List<EventShortDto> getAll(@PathVariable Long userId,
                                      @RequestParam(required = false) Integer from,
                                      @RequestParam(required = false) Integer size) {

        int safeFrom = (from == null) ? 0 : from;
        int safeSize = (size == null) ? 10 : size;

        return eventServiceImpl.getUserEvents(userId, safeFrom, safeSize);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable Long userId, @Valid @RequestBody NewEventDto newEvent) {
        return eventServiceImpl.create(userId, newEvent);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getById(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventServiceImpl.getUserEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable Long userId, @PathVariable Long eventId, @Valid @RequestBody UpdateEventUserRequest update) {
        return eventServiceImpl.updateUser(userId, eventId, update);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequests(
            @PathVariable Long userId,
            @PathVariable Long eventId
    ) {
        return requestServiceImpl.getByEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult changeRequestStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest update
    ) {
        return requestServiceImpl.changeStatus(userId, eventId, update);
    }
}

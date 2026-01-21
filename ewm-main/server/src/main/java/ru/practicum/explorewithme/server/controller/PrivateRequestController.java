package ru.practicum.explorewithme.server.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.server.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.server.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.server.dto.request.ParticipationRequestDto;
import ru.practicum.explorewithme.server.service.RequestService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class PrivateRequestController {

    private final RequestService requestService;

    // GET /users/{userId}/requests
    // Получение информации о заявках текущего пользователя на участие в чужих событиях
    @GetMapping("/users/{userId}/requests")
    public List<ParticipationRequestDto> getUserRequests(@PathVariable @Positive Long userId) {
        log.info("GET /users/{}/requests", userId);
        return requestService.getUserRequests(userId);
    }

    // POST /users/{userId}/requests
    // Добавление запроса от текущего пользователя на участие в событии
    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(
            @PathVariable @Positive Long userId,
            @RequestParam @Positive Long eventId) {
        log.info("POST /users/{}/requests?eventId={}", userId, eventId);
        return requestService.createRequest(userId, eventId);
    }

    // PATCH /users/{userId}/requests/{requestId}/cancel
    // Отмена своего запроса на участие в событии
    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long requestId) {
        log.info("PATCH /users/{}/requests/{}/cancel", userId, requestId);
        return requestService.cancelRequest(userId, requestId);
    }

    // GET /users/{userId}/events/{eventId}/requests
    // Получение информации о запросах на участие в событии текущего пользователя
    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getEventParticipants(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId) {
        log.info("GET /users/{}/events/{}/requests", userId, eventId);
        return requestService.getEventParticipants(userId, eventId);
    }

    // PATCH /users/{userId}/events/{eventId}/requests
    // Изменение статуса заявок на участие в событии текущего пользователя
    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatus(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        log.info("PATCH /users/{}/events/{}/requests", userId, eventId);
        return requestService.updateRequestStatus(userId, eventId, updateRequest);
    }
}
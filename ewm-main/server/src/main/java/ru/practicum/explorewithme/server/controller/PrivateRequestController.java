package ru.practicum.explorewithme.server.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.request.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.server.service.RequestServiceImpl;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
@Validated
public class PrivateRequestController {

    private final RequestServiceImpl requestServiceImpl;

    // GET /users/{userId}/requests
    // Получение информации о заявках текущего пользователя на участие в чужих событиях
    @GetMapping
    public List<ParticipationRequestDto> getAll(@PathVariable @Positive Long userId) {
        log.info("[PrivateRequestController] GET /users/{}/requests", userId);

        List<ParticipationRequestDto> requests = requestServiceImpl.getByUser(userId);
        log.debug("[PrivateRequestController] Найдено {} заявок для пользователя {}",
                requests.size(), userId);

        return requests;
    }

    // POST /users/{userId}/requests
    // Добавление запроса от текущего пользователя на участие в событии
    @PostMapping
    public ResponseEntity<ParticipationRequestDto> create(
            @PathVariable @Positive Long userId,
            @RequestParam(required = false) @Positive Long eventId) {

        log.info("[PrivateRequestController] POST /users/{}/requests?eventId={}", userId, eventId);

        ParticipationRequestDto dto = requestServiceImpl.create(userId, eventId);
        log.info("[PrivateRequestController] Заявка создана: id={}, userId={}, eventId={}",
                dto.getId(), userId, eventId);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // PATCH /users/{userId}/requests/{requestId}/cancel
    // Отмена своего запроса на участие в событии
    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancel(@PathVariable @Positive Long userId,
                                          @PathVariable @Positive Long requestId) {

        log.info("[PrivateRequestController] PATCH /users/{}/requests/{}/cancel", userId, requestId);

        ParticipationRequestDto dto = requestServiceImpl.cancel(userId, requestId);
        log.info("[PrivateRequestController] Заявка отменена: requestId={}, userId={}",
                requestId, userId);

        return dto;
    }

    // GET /users/{userId}/requests/events/{eventId}
    // Получение информации о заявках на участие в событии текущего пользователя
    @GetMapping("/events/{eventId}")
    public List<ParticipationRequestDto> getByEvent(@PathVariable @Positive Long userId,
                                                    @PathVariable @Positive Long eventId) {

        log.info("[PrivateRequestController] GET /users/{}/requests/events/{}", userId, eventId);

        List<ParticipationRequestDto> requests = requestServiceImpl.getByEvent(userId, eventId);
        log.debug("[PrivateRequestController] Найдено {} заявок для события {} пользователя {}",
                requests.size(), eventId, userId);

        return requests;
    }

    // PATCH /users/{userId}/events/{eventId}/requests
    // Изменение статуса заявок на участие в событии текущего пользователя
    @PatchMapping("/events/{eventId}/requests")
    public EventRequestStatusUpdateResult changeStatus(@PathVariable @Positive Long userId,
                                                       @PathVariable @Positive Long eventId,
                                                       @Valid @RequestBody EventRequestStatusUpdateRequest update) {

        log.info("[PrivateRequestController] PATCH /users/{}/events/{}/requests", userId, eventId);
        log.debug("[PrivateRequestController] Обновление статусов: status={}, requestIds={}",
                update.getStatus(), update.getRequestIds());

        EventRequestStatusUpdateResult result = requestServiceImpl.changeStatus(userId, eventId, update);

        log.info("[PrivateRequestController] Статусы обновлены: подтверждено={}, отклонено={}, eventId={}",
                result.getConfirmedRequests().size(), result.getRejectedRequests().size(), eventId);

        return result;
    }
}
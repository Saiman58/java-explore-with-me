package ru.practicum.explorewithme.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.request.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.server.service.RequestServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
@Validated
public class PrivateRequestController {

    private final RequestServiceImpl requestServiceImpl;

    @GetMapping
    public List<ParticipationRequestDto> getAll(@PathVariable Long userId) {
        return requestServiceImpl.getByUser(userId);
    }

    @PostMapping
    public ResponseEntity<ParticipationRequestDto> create(
            @PathVariable Long userId,
            @RequestParam(required = false) Long eventId) {

        ParticipationRequestDto dto = requestServiceImpl.create(userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancel(@PathVariable Long userId,
                                          @PathVariable Long requestId) {
        return requestServiceImpl.cancel(userId, requestId);
    }

    @GetMapping("/events/{eventId}")
    public List<ParticipationRequestDto> getByEvent(@PathVariable Long userId,
                                                    @PathVariable Long eventId) {
        return requestServiceImpl.getByEvent(userId, eventId);
    }

    @PatchMapping("/events/{eventId}/requests")
    public EventRequestStatusUpdateResult changeStatus(@PathVariable Long userId,
                                                       @PathVariable Long eventId,
                                                       @Valid @RequestBody EventRequestStatusUpdateRequest update) {
        return requestServiceImpl.changeStatus(userId, eventId, update);
    }
}
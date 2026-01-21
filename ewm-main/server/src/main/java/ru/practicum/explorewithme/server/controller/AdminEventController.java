package ru.practicum.explorewithme.server.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.server.dto.event.EventFullDto;
import ru.practicum.explorewithme.server.dto.event.UpdateEventAdminRequest;
import ru.practicum.explorewithme.server.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
@Validated
public class AdminEventController {

    private final EventService eventService;

    // GET /admin/events
    // Поиск событий
    @GetMapping
    public List<EventFullDto> getEventsByAdmin(
            @RequestParam(required = false) List<Long> users,
            @RequestParam(required = false) List<String> states,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("GET /admin/events - поиск событий администратором");

        return eventService.getEventsByAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    // PATCH /admin/events/{eventId}
    // Редактирование данных события и его статуса (отклонение/публикация)
    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByAdmin(
            @PathVariable @Positive Long eventId,
            @Valid @RequestBody UpdateEventAdminRequest updateRequest) {

        log.info("PATCH /admin/events/{} - обновление события администратором", eventId);

        return eventService.updateEventByAdmin(eventId, updateRequest);
    }
}
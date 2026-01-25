package ru.practicum.explorewithme.server.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.UpdateEventAdminRequest;
import ru.practicum.explorewithme.server.entity.EventState;
import ru.practicum.explorewithme.server.service.EventServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Validated
public class AdminEventController {
    private final EventServiceImpl eventServiceImpl;

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    // GET /admin/events
    // Поиск событий с возможностью фильтрации
    @GetMapping
    public List<EventFullDto> getAll(@RequestParam(required = false) List<Long> users,
                                     @RequestParam(required = false) List<EventState> states,
                                     @RequestParam(required = false) List<Long> categories,
                                     @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime rangeStart,
                                     @RequestParam(required = false) @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime rangeEnd,
                                     @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                     @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("[AdminEventController] GET /admin/events?users={}&states={}&categories={}&from={}&size={}",
                users, states, categories, from, size);
        log.debug("[AdminEventController] Диапазон дат: start={}, end={}", rangeStart, rangeEnd);

        List<EventFullDto> events = eventServiceImpl.getAdminEvents(users, states, categories,
                rangeStart, rangeEnd, from, size);

        log.debug("[AdminEventController] Найдено {} событий для администратора", events.size());
        return events;
    }

    // PATCH /admin/events/{eventId}
    // Редактирование данных события и его статуса
    @PatchMapping("/{eventId}")
    public EventFullDto update(@PathVariable @Positive Long eventId,
                               @Valid @RequestBody UpdateEventAdminRequest update) {

        log.info("[AdminEventController] PATCH /admin/events/{}", eventId);
        log.debug("[AdminEventController] Обновление события администратором: stateAction={}, title={}",
                update.getStateAction(), update.getTitle());

        EventFullDto updatedEvent = eventServiceImpl.updateAdmin(eventId, update);

        log.info("[AdminEventController] Событие обновлено администратором: id={}, newState={}",
                eventId, updatedEvent.getState());
        return updatedEvent;
    }
}
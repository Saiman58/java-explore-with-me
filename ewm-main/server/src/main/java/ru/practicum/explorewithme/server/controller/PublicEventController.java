package ru.practicum.explorewithme.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.event.dto.EventFullDto;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.server.service.EventServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Validated
public class PublicEventController {

    private final EventServiceImpl eventServiceImpl;
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    // GET /events
    // Получение событий с возможностью фильтрации
    @GetMapping
    public List<EventShortDto> getAll(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime rangeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = DATE_TIME_PATTERN) LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(defaultValue = "EVENT_DATE") String sort,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size,
            HttpServletRequest request) {

        log.info("[PublicEventController] GET /events - поиск опубликованных событий");
        log.debug("[PublicEventController] Параметры фильтрации: text='{}', categories={}, paid={}, onlyAvailable={}, sort={}",
                text, categories, paid, onlyAvailable, sort);
        log.debug("[PublicEventController] Диапазон дат: {} - {}, пагинация: from={}, size={}, ip={}",
                rangeStart, rangeEnd, from, size, request.getRemoteAddr());

        List<EventShortDto> events = eventServiceImpl.getPublicEvents(
                text,
                categories,
                paid,
                rangeStart,
                rangeEnd,
                onlyAvailable,
                sort,
                from,
                size,
                request.getRemoteAddr()
        );

        log.debug("[PublicEventController] Найдено {} опубликованных событий", events.size());
        return events;
    }

    // GET /events/{id}
    // Получение подробной информации об опубликованном событии по его идентификатору
    @GetMapping("/{id}")
    public EventFullDto getById(@PathVariable @Positive Long id, HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        log.info("[PublicEventController] GET /events/{} - получение опубликованного события, ip={}",
                id, remoteAddr);

        EventFullDto event = eventServiceImpl.getPublicEvent(id, remoteAddr);
        log.debug("[PublicEventController] Событие найдено: id={}, title='{}', views={}",
                id, event.getTitle(), event.getViews());

        return event;
    }
}
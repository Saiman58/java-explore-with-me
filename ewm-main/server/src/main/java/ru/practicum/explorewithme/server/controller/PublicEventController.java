package ru.practicum.explorewithme.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.server.dto.event.EventFullDto;
import ru.practicum.explorewithme.server.dto.event.EventShortDto;
import ru.practicum.explorewithme.server.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
@Validated
public class PublicEventController {

    private final EventService eventService;

    // GET /events
    // Получение событий с возможностью фильтрации
    @GetMapping
    public List<EventShortDto> getPublishedEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(defaultValue = "EVENT_DATE") String sort,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("GET /events - поиск опубликованных событий");
        log.debug("Параметры фильтрации: text={}, categories={}, paid={}", text, categories, paid);
        log.debug("Диапазон дат: {} - {}, пагинация: from={}, size={}", rangeStart, rangeEnd, from, size);

        return eventService.getPublishedEvents(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size);
    }

    // GET /events/{id}
    // Получение подробной информации об опубликованном событии по его идентификатору
    @GetMapping("/{id}")
    public EventFullDto getPublishedEvent(@PathVariable @Positive Long id, HttpServletRequest request) {
        log.info("GET /events/{} - получение опубликованного события", id);


        String remoteAddr = request.getRemoteAddr();
        return eventService.getPublishedEvent(id, remoteAddr);
    }
}
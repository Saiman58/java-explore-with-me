package ru.practicum.explorewithme.server.service;

import ru.practicum.explorewithme.event.dto.*;
import ru.practicum.explorewithme.server.entity.Event;
import ru.practicum.explorewithme.server.entity.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    // Создать событие
    EventFullDto create(Long userId, NewEventDto newEvent);

    // Обновить событие пользователем
    EventFullDto updateUser(Long userId, Long eventId, UpdateEventUserRequest update);

    // Обновить событие администратором
    EventFullDto updateAdmin(Long eventId, UpdateEventAdminRequest update);

    // Получить события пользователя
    List<EventShortDto> getUserEvents(Long userId, Integer from, Integer size);

    // Получить события администратором
    List<EventFullDto> getAdminEvents(List<Long> users, List<EventState> states, List<Long> categories,
                                      LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    // Получить публичные события
    List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
                                        String sort, Integer from, Integer size, String remoteAddr);

    // Получить публичное событие по ID
    EventFullDto getPublicEvent(Long eventId, String remoteAddr);

    // Получить событие пользователя по ID
    EventFullDto getUserEvent(Long userId, Long eventId);

    // Получить событие по ID (entity)
    Event getById(Long eventId);

    // Получить количество подтверждённых запросов
    Long getConfirmedCount(Long eventId);

    // Получить количество просмотров
    Long getViewsForEvent(Long eventId);
}
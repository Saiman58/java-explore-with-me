package ru.practicum.explorewithme.server.service;

import ru.practicum.explorewithme.server.dto.event.EventFullDto;
import ru.practicum.explorewithme.server.dto.event.EventShortDto;
import ru.practicum.explorewithme.server.dto.event.NewEventDto;
import ru.practicum.explorewithme.server.dto.event.UpdateEventAdminRequest;
import ru.practicum.explorewithme.server.dto.event.UpdateEventUserRequest;
import ru.practicum.explorewithme.server.entity.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    //PRIVATE API (пользовательские операции)

    //Создание нового события
    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    // Получение событий текущего пользователя
    List<EventFullDto> getUserEvents(Long userId, int from, int size);

    //Получение полной информации о событии пользователя
    EventFullDto getUserEvent(Long userId, Long eventId);

    // Изменение события пользователем
    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest updateRequest);

    // PUBLIC API (публичные операции)

    //Получение событий с возможностью фильтрации
    List<EventShortDto> getPublishedEvents(String text, List<Long> categories, Boolean paid,
                                           LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                           Boolean onlyAvailable, String sort,
                                           int from, int size);

    // Получение подробной информации об опубликованном событии
    EventFullDto getPublishedEvent(Long eventId, String remoteAddr);

    // ========== ADMIN API (административные операции) ==========

    // Поиск событий администратором
    List<EventFullDto> getEventsByAdmin(List<Long> users, List<String> states,
                                        List<Long> categories, LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd, int from, int size);

    // Редактирование данных события администратором
    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest);

    //Получение количества просмотров события
    Long getViewsForEvent(Long eventId);

    //Получение количества подтвержденных заявок на событие
    Long getConfirmedRequests(Long eventId);
}

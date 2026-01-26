package ru.practicum.explorewithme.server.service;

import ru.practicum.explorewithme.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.request.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    // Создать запрос на участие
    ParticipationRequestDto create(Long userId, Long eventId);

    // Отменить запрос
    ParticipationRequestDto cancel(Long userId, Long requestId);

    // Получить запросы пользователя
    List<ParticipationRequestDto> getByUser(Long userId);

    // Получить запросы для события
    List<ParticipationRequestDto> getByEvent(Long userId, Long eventId);

    // Изменить статусы запросов
    EventRequestStatusUpdateResult changeStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest update);

    // Получить количество подтверждённых запросов
    Long getConfirmedCount(Long eventId);
}
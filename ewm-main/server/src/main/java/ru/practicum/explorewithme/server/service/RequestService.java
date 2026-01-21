package ru.practicum.explorewithme.server.service;

import ru.practicum.explorewithme.server.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.server.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.server.dto.request.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

    // Получить заявки текущего пользователя
    List<ParticipationRequestDto> getUserRequests(Long userId);

    // Создать заявку на участие в событии
    ParticipationRequestDto createRequest(Long userId, Long eventId);

    // Отменить свою заявку
    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    // Получить заявки на участие в событии пользователя
    List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId);

    // Изменить статус заявок (подтвердить/отклонить)
    EventRequestStatusUpdateResult updateRequestStatus(
            Long userId,
            Long eventId,
            EventRequestStatusUpdateRequest updateRequest);
}

package ru.practicum.explorewithme.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.server.entity.ParticipationRequest;
import ru.practicum.explorewithme.server.entity.RequestStatus;

import java.util.List;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

    // Поиск всех запросов пользователя
    List<ParticipationRequest> findAllByRequesterId(Long userId);

    // Поиск всех запросов для события
    List<ParticipationRequest> findAllByEventId(Long eventId);

    // Подсчет количества запросов для события с определенным статусом
    Long countByEventIdAndStatus(Long eventId, RequestStatus status);

    // Подсчет подтвержденных запросов для события (кастомный запрос)
    @Query("SELECT COUNT(pr) FROM ParticipationRequest pr " +
            "WHERE pr.event.id = :eventId AND pr.status = 'CONFIRMED'")
    Long countConfirmedRequests(@Param("eventId") Long eventId);

    // Проверка существования запроса пользователя на событие
    boolean existsByEventIdAndRequesterId(Long eventId, Long userId);

    // Поиск запросов по списку ID
    List<ParticipationRequest> findAllByIdIn(List<Long> requestIds);
}
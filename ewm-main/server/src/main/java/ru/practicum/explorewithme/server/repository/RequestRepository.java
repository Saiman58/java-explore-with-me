package ru.practicum.explorewithme.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.server.entity.Request;
import ru.practicum.explorewithme.server.entity.RequestStatus;

import java.util.List;

// Репозиторий заявок на участие
public interface RequestRepository extends JpaRepository<Request, Long> {

    // Получить все заявки для события
    List<Request> findAllByEventId(Long eventId);

    // Получить заявки пользователя для списка событий
    List<Request> findAllByRequesterIdAndEventIdIn(Long userId, List<Long> eventIds);

    // Получить заявки для события по статусу
    List<Request> findAllByEventIdAndStatus(Long eventId, RequestStatus status);

    // Получить все заявки пользователя
    List<Request> findAllByRequesterId(Long userId);

    // Подсчитать количество заявок для события по статусу
    Long countByEventIdAndStatus(Long eventId, RequestStatus status);

    // Проверить существование заявки пользователя на событие
    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);
}
package ru.practicum.explorewithme.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.server.entity.Request;
import ru.practicum.explorewithme.server.entity.RequestStatus;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByEventId(Long eventId);

    List<Request> findAllByRequesterIdAndEventIdIn(Long userId, List<Long> eventIds);

    List<Request> findAllByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findAllByRequesterId(Long userId);

    Long countByEventIdAndStatus(Long eventId, RequestStatus status);

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);
}
package ru.practicum.explorewithme.server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.server.entity.Event;
import ru.practicum.explorewithme.server.entity.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    boolean existsByCategoryId(Long categoryId);

    @Query("""
    SELECT e FROM Event e
    WHERE LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%'))
       OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))
       OR LOWER(e.title) LIKE LOWER(CONCAT('%', :text, '%'))
      AND (:categories IS NULL OR e.category.id IN :categories)
      AND e.state = :publishedState
      AND (:paid IS NULL OR e.paid = :paid)
      AND e.eventDate BETWEEN :rangeStart AND :rangeEnd
      AND (:onlyAvailable IS NULL OR e.participantLimit = 0
           OR e.participantLimit > (SELECT COUNT(r) FROM Request r WHERE r.event = e AND r.status = 'CONFIRMED'))
    ORDER BY e.eventDate DESC
""")
    Page<Event> findPublicEvents(
            @Param("text") String text,
            @Param("categories") List<Long> categories,
            @Param("paid") Boolean paid,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            @Param("onlyAvailable") Boolean onlyAvailable,
            @Param("publishedState") EventState publishedState,
            Pageable pageable);

    @Query("SELECT e FROM Event e WHERE (:users IS NULL OR e.initiator.id IN :users) " +
            "AND (:states IS NULL OR e.state IN :states) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND e.eventDate BETWEEN :rangeStart AND :rangeEnd")
    List<Event> findAdminEvents(@Param("users") List<Long> users,
                                @Param("states") List<EventState> states,
                                @Param("categories") List<Long> categories,
                                @Param("rangeStart") LocalDateTime rangeStart,
                                @Param("rangeEnd") LocalDateTime rangeEnd,
                                Pageable pageable);

    List<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    @Query("SELECT e FROM Event e JOIN FETCH e.initiator WHERE e.id = :id")
    Optional<Event> findByIdWithInitiator(@Param("id") Long id);
}

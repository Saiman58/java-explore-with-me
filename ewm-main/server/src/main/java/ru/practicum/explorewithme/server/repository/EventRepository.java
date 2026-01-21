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

    // PRIVATE API: События конкретного пользователя с пагинацией
    Page<Event> findByInitiatorId(Long initiatorId, Pageable pageable);

    // PRIVATE API: Конкретное событие пользователя
    Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);

    // PUBLIC API: Только опубликованные события
    Page<Event> findByState(EventState state, Pageable pageable);

    // PUBLIC API: Поиск опубликованных событий с фильтрами
    @Query("SELECT e FROM Event e WHERE " +
            "e.state = 'PUBLISHED' AND " +
            "(:text IS NULL OR LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))) AND " +
            "(:categories IS NULL OR e.category.id IN :categories) AND " +
            "(:paid IS NULL OR e.paid = :paid) AND " +
            "(:rangeStart IS NULL OR e.eventDate >= :rangeStart) AND " +
            "(:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)")
    Page<Event> findPublishedEventsWithFilters(
            @Param("text") String text,
            @Param("categories") List<Long> categories,
            @Param("paid") Boolean paid,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable);

    // PUBLIC API: Событие по ID (только опубликованное)
    Optional<Event> findByIdAndState(Long id, EventState state);

    // ADMIN API: Поиск событий с фильтрами для администратора
    @Query("SELECT e FROM Event e WHERE " +
            "(:users IS NULL OR e.initiator.id IN :users) AND " +
            "(:states IS NULL OR e.state IN :states) AND " +
            "(:categories IS NULL OR e.category.id IN :categories) AND " +
            "(:rangeStart IS NULL OR e.eventDate >= :rangeStart) AND " +
            "(:rangeEnd IS NULL OR e.eventDate <= :rangeEnd)")
    Page<Event> findEventsByAdminFilters(
            @Param("users") List<Long> users,
            @Param("states") List<EventState> states,
            @Param("categories") List<Long> categories,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable);

    // Проверка существования событий по категории
    boolean existsByCategoryId(Long categoryId);
}
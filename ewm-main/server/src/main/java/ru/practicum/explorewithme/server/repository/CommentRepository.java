package ru.practicum.explorewithme.server.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.server.entity.Comment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Найти все комментарии события с пагинацией
    List<Comment> findByEventIdOrderByCreatedAtDesc(Long eventId, Pageable pageable);

    // Найти все комментарии пользователя
    List<Comment> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

    // Проверить, существует ли комментарий у пользователя
    boolean existsByIdAndAuthorId(Long commentId, Long authorId);

    // Найти комментарий с проверкой автора
    Optional<Comment> findByIdAndAuthorId(Long commentId, Long authorId);

    // Для админки: найти все комментарии с фильтрацией
    @Query("SELECT c FROM Comment c WHERE " +
            "(:eventId IS NULL OR c.event.id = :eventId) AND " +
            "(:authorId IS NULL OR c.author.id = :authorId)")
    List<Comment> findAllByFilters(@Param("eventId") Long eventId,
                                   @Param("authorId") Long authorId,
                                   Pageable pageable);

    // Количество комментариев события
    long countByEventId(Long eventId);
}
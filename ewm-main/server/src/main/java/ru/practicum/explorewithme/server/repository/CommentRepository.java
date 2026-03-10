package ru.practicum.explorewithme.server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.server.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Количество комментариев события
    long countByEventId(Long eventId);

    Page<Comment> findAllByEventId(Long eventId, Pageable pageable);

}
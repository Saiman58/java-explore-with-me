package ru.practicum.explorewithme.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.comment.dto.CommentDto;
import ru.practicum.explorewithme.comment.dto.NewCommentDto;
import ru.practicum.explorewithme.server.entity.Comment;
import ru.practicum.explorewithme.server.entity.Event;
import ru.practicum.explorewithme.server.entity.EventState;
import ru.practicum.explorewithme.server.entity.User;
import ru.practicum.explorewithme.server.exception.EntityNotFoundException;
import ru.practicum.explorewithme.server.mapper.CommentMapper;
import ru.practicum.explorewithme.server.repository.CommentRepository;
import ru.practicum.explorewithme.server.repository.EventRepository;
import ru.practicum.explorewithme.server.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    private static final Long ADMIN_ID = 1L;

    private static final String USER_NOT_FOUND = "Пользователь с id=%d не найден";
    private static final String EVENT_NOT_FOUND = "Событие с id=%d не найдено";
    private static final String COMMENT_NOT_FOUND = "Комментарий с id=%d не найден";
    private static final String COMMENT_EVENT_MISMATCH = "Комментарий не относится к указанному событию";
    private static final String COMMENT_ONLY_AUTHOR_EDIT = "Доступ запрещён: только автор комментария может редактировать его";
    private static final String COMMENT_ONLY_AUTHOR_DELETE = "Доступ запрещён: только автор комментария или администратор может выполнить операцию";
    private static final String COMMENT_ONLY_PUBLISHED_EVENT = "Комментарии возможны только для опубликованных событий";

    private static final String COMMENT_CREATE_LOG = "Комментарий создан: id={}, eventId={}, userId={}";
    private static final String COMMENT_UPDATE_LOG = "Комментарий обновлён: id={}, eventId={}, userId={}";
    private static final String COMMENT_DELETE_LOG = "Комментарий удалён: id={}, eventId={}, userId={}";

    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto dto) {

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(USER_NOT_FOUND, userId)));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(EVENT_NOT_FOUND, eventId)));

        if (event.getState() != EventState.PUBLISHED) {
            throw new IllegalStateException(COMMENT_ONLY_PUBLISHED_EVENT);
        }

        Comment comment = commentMapper.toEntity(dto, author, event);
        comment = commentRepository.save(comment);

        log.info(COMMENT_CREATE_LOG, comment.getId(), eventId, userId);

        return commentMapper.toDto(comment);
    }

    @Transactional
    public CommentDto updateComment(Long eventId, Long commentId, Long userId, NewCommentDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(USER_NOT_FOUND, userId)));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(COMMENT_NOT_FOUND, commentId)));

        if (!comment.getEvent().getId().equals(eventId)) {
            throw new EntityNotFoundException(COMMENT_EVENT_MISMATCH);
        }

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new IllegalStateException(COMMENT_ONLY_AUTHOR_EDIT);
        }

        comment.setText(dto.getText());
        comment = commentRepository.save(comment);

        log.info(COMMENT_UPDATE_LOG, commentId, eventId, userId);

        return commentMapper.toDto(comment);
    }

    public List<CommentDto> getCommentsByEvent(Long eventId, Integer from, Integer size) {

        eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(EVENT_NOT_FOUND, eventId)));

        Page<Comment> page = commentRepository.findAllByEventId(eventId, PageRequest.of(from / size, size));

        return commentMapper.toDtos(page.getContent());
    }

    @Transactional
    public void deleteComment(Long eventId, Long commentId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(USER_NOT_FOUND, userId)));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(COMMENT_NOT_FOUND, commentId)));

        if (!comment.getEvent().getId().equals(eventId)) {
            throw new EntityNotFoundException(COMMENT_EVENT_MISMATCH);
        }

        if (!ADMIN_ID.equals(userId) && !comment.getAuthor().getId().equals(userId)) {
            throw new IllegalStateException(COMMENT_ONLY_AUTHOR_DELETE);
        }

        commentRepository.delete(comment);

        log.info(COMMENT_DELETE_LOG, commentId, eventId, userId);
    }

    public Long getCommentCount(Long eventId) {
        return commentRepository.countByEventId(eventId);
    }
}
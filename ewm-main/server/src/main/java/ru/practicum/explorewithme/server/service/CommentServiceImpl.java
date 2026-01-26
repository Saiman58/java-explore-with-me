package ru.practicum.explorewithme.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public CommentDto createComment(Long userId, NewCommentDto newCommentDto) {
        log.info("[CommentService] Создание комментария пользователем {} к событию {}",
                userId, newCommentDto.getEventId());


        User author = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("[CommentService] Пользователь не найден: id={}", userId);
                    return new EntityNotFoundException(String.format(USER_NOT_FOUND, userId));
                });

        log.debug("[CommentService] Пользователь найден: id={}, name={}",
                author.getId(), author.getName());

        Event event = eventRepository.findById(newCommentDto.getEventId())
                .orElseThrow(() -> {
                    log.error("[CommentService] Событие не найдено: id={}", newCommentDto.getEventId());
                    return new EntityNotFoundException(String.format(EVENT_NOT_FOUND, newCommentDto.getEventId()));
                });

        if (!event.getState().equals(EventState.PUBLISHED)) {
            log.warn("[CommentService] Попытка комментирования неопубликованного события: id={}, state={}",
                    event.getId(), event.getState());
            throw new IllegalStateException(EVENT_NOT_PUBLISHED);
        }
        log.debug("[CommentService] Событие найдено и опубликовано: id={}, title={}",
                event.getId(), event.getTitle());


        Comment comment = commentMapper.toEntity(newCommentDto, author, event);
        Comment savedComment = commentRepository.save(comment);

        log.info("[CommentService] Комментарий создан: id={}, author={}, event={}, textLength={}",
                savedComment.getId(), author.getId(), event.getId(), newCommentDto.getText().length());

        return commentMapper.toDto(savedComment);
    }


    @Override
    public List<CommentDto> getEventComments(Long eventId, Integer from, Integer size) {
        log.info("[CommentService] Получение комментариев события: id={}, from={}, size={}",
                eventId, from, size);

        // Проверяем, что событие существует и опубликовано
        eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> {
                    log.error("[CommentService] Опубликованное событие не найдено: id={}", eventId);
                    return new EntityNotFoundException(String.format(PUBLISHED_EVENT_NOT_FOUND, eventId));
                });

        Pageable pageable = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findByEventIdOrderByCreatedAtDesc(eventId, pageable);

        log.debug("[CommentService] Найдено {} комментариев для события id={}",
                comments.size(), eventId);

        return commentMapper.toDtos(comments);
    }

    @Override
    public List<CommentDto> getUserComments(Long userId, Integer from, Integer size) {
        log.info("[CommentService] Получение комментариев пользователя: id={}, from={}, size={}",
                userId, from, size);

        // Проверяем существование пользователя
        if (!userRepository.existsById(userId)) {
            log.error("[CommentService] Пользователь не найден: id={}", userId);
            throw new EntityNotFoundException(String.format(USER_NOT_FOUND, userId));
        }

        Pageable pageable = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findByAuthorIdOrderByCreatedAtDesc(userId, pageable);

        log.debug("[CommentService] Найдено {} комментариев пользователя id={}",
                comments.size(), userId);

        return commentMapper.toDtos(comments);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        log.info("[CommentService] Удаление комментария: id={}, пользователем: id={}",
                commentId, userId);

        // Проверяем, что комментарий существует и принадлежит пользователю
        if (!commentRepository.existsByIdAndAuthorId(commentId, userId)) {
            log.error("[CommentService] Комментарий не найден или не принадлежит пользователю: " +
                    "commentId={}, userId={}", commentId, userId);
            throw new EntityNotFoundException(
                    String.format("%s или %s",
                            String.format(COMMENT_NOT_FOUND, commentId),
                            String.format(COMMENT_NOT_BELONGS, commentId)));
        }

        commentRepository.deleteById(commentId);
        log.info("[CommentService] Комментарий удален: id={}", commentId);
    }

    @Override
    public List<CommentDto> getAllComments(Long eventId, Long authorId, Integer from, Integer size) {
        log.info("[CommentService] Получение всех комментариев с фильтрами: " +
                "eventId={}, authorId={}, from={}, size={}", eventId, authorId, from, size);

        Pageable pageable = PageRequest.of(from / size, size);
        List<Comment> comments = commentRepository.findAllByFilters(eventId, authorId, pageable);

        log.debug("[CommentService] Найдено {} комментариев с фильтрами eventId={}, authorId={}",
                comments.size(), eventId, authorId);

        return commentMapper.toDtos(comments);
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        log.info("[CommentService] Админ удаляет комментарий: id={}", commentId);

        if (!commentRepository.existsById(commentId)) {
            log.error("[CommentService] Комментарий не найден для удаления админом: id={}", commentId);
            throw new EntityNotFoundException(String.format(COMMENT_NOT_FOUND, commentId));
        }

        commentRepository.deleteById(commentId);
        log.info("[CommentService] Комментарий удален администратором: id={}", commentId);
    }

    // Дополнительный метод: получение комментария по ID?!?!?!?
    /*
    @Override
    public CommentDto getCommentById(Long commentId) {
        log.debug("[CommentService] Получение комментария: id={}", commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.error("[CommentService] Комментарий не найден: id={}", commentId);
                    return new EntityNotFoundException(String.format(COMMENT_NOT_FOUND, commentId));
                });

        log.debug("[CommentService] Комментарий найден: id={}, author={}, event={}",
                comment.getId(), comment.getAuthor().getId(), comment.getEvent().getId());

        return commentMapper.toDto(comment);
    }
    */

    private static final String USER_NOT_FOUND = "Пользователь с id=%d не найден";
    private static final String EVENT_NOT_FOUND = "Событие с id=%d не найдено";
    private static final String PUBLISHED_EVENT_NOT_FOUND = "Опубликованное событие с id=%d не найдено";
    private static final String COMMENT_NOT_FOUND = "Комментарий с id=%d не найден";
    private static final String COMMENT_NOT_BELONGS = "Комментарий с id=%d не принадлежит пользователю";
    private static final String EVENT_NOT_PUBLISHED = "Нельзя комментировать неопубликованное событие";
}


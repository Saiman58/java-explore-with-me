package ru.practicum.explorewithme.server.service;

import ru.practicum.explorewithme.comment.dto.CommentDto;
import ru.practicum.explorewithme.comment.dto.NewCommentDto;
import ru.practicum.explorewithme.server.entity.Event;
import ru.practicum.explorewithme.server.entity.EventState;

import java.util.List;
import java.util.Optional;

public interface CommentService {

    /**
     * Создать новый комментарий
     * @param userId ID пользователя-автора
     * @param newCommentDto данные нового комментария
     * @return созданный комментарий
     */
    CommentDto createComment(Long userId, NewCommentDto newCommentDto);

    /**
     * Получить комментарии события (публичный доступ)
     * @param eventId ID события
     * @param from начальный элемент
     * @param size количество элементов
     * @return список комментариев
     */
    List<CommentDto> getEventComments(Long eventId, Integer from, Integer size);

    /**
     * Получить комментарии пользователя
     * @param userId ID пользователя
     * @param from начальный элемент
     * @param size количество элементов
     * @return список комментариев пользователя
     */
    List<CommentDto> getUserComments(Long userId, Integer from, Integer size);

    /**
     * Удалить комментарий (пользователем)
     * @param userId ID пользователя
     * @param commentId ID комментария
     */
    void deleteComment(Long userId, Long commentId);

    /**
     * Получить все комментарии с фильтрами (для админа)
     * @param eventId ID события (опционально)
     * @param authorId ID автора (опционально)
     * @param from начальный элемент
     * @param size количество элементов
     * @return список комментариев
     */
    List<CommentDto> getAllComments(Long eventId, Long authorId, Integer from, Integer size);

    /**
     * Удалить комментарий (администратором)
     * @param commentId ID комментария
     */
    void deleteCommentByAdmin(Long commentId);

}
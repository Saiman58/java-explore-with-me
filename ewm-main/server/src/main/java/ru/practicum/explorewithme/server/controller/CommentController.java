package ru.practicum.explorewithme.server.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.comment.dto.CommentDto;
import ru.practicum.explorewithme.comment.dto.NewCommentDto;
import ru.practicum.explorewithme.server.service.CommentService;


import java.util.List;

@RestController
@RequestMapping("/events/{eventId}/comments")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable Long eventId,
                                    @RequestParam Long userId,
                                    @RequestBody @Valid NewCommentDto dto) {
        log.info("Создание комментария: eventId={}, userId={}", eventId, userId);
        return commentService.createComment(userId, eventId, dto);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@PathVariable Long eventId,
                                    @PathVariable Long commentId,
                                    @RequestParam Long userId,
                                    @RequestBody @Valid NewCommentDto dto) {
        log.info("Редактирование комментария: commentId={}, eventId={}, userId={}", commentId, eventId, userId);
        return commentService.updateComment(eventId, commentId, userId, dto);
    }

    @GetMapping
    public List<CommentDto> getComments(@PathVariable Long eventId,
                                        @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                        @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Получение комментариев: eventId={}, from={}, size={}", eventId, from, size);
        return commentService.getCommentsByEvent(eventId, from, size);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long eventId,
                              @PathVariable Long commentId,
                              @RequestParam Long userId) {
        log.info("Удаление комментария: commentId={}, eventId={}, userId={}", commentId, eventId, userId);
        commentService.deleteComment(eventId, commentId, userId);
    }
}

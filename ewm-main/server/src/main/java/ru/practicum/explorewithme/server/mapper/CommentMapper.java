package ru.practicum.explorewithme.server.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.comment.dto.CommentDto;
import ru.practicum.explorewithme.comment.dto.NewCommentDto;
import ru.practicum.explorewithme.event.dto.EventShortDto;
import ru.practicum.explorewithme.user.dto.UserShortDto;
import ru.practicum.explorewithme.server.entity.Comment;
import ru.practicum.explorewithme.server.entity.Event;
import ru.practicum.explorewithme.server.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommentMapper {

    public Comment toEntity(NewCommentDto dto, User author, Event event) {
        return Comment.builder()
                .text(dto.getText())
                .author(author)
                .event(event)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public CommentDto toDto(Comment comment) {
        // Создаем UserShortDto из автора
        UserShortDto authorDto = UserShortDto.builder()
                .id(comment.getAuthor().getId())
                .name(comment.getAuthor().getName())
                .build();

        // Создаем EventShortDto из события
        EventShortDto eventDto = EventShortDto.builder()
                .id(comment.getEvent().getId())
                .annotation(comment.getEvent().getAnnotation())
                .title(comment.getEvent().getTitle())
                .eventDate(comment.getEvent().getEventDate())
                .paid(comment.getEvent().getPaid())
                .build();

        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .createdAt(comment.getCreatedAt())
                .author(authorDto)
                .event(eventDto)
                .build();
    }

    public List<CommentDto> toDtos(List<Comment> comments) {
        return comments.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}

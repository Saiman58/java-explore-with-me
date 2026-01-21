package ru.practicum.explorewithme.server.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.server.dto.event.*;
import ru.practicum.explorewithme.server.entity.Event;
import ru.practicum.explorewithme.server.entity.EventState;
import ru.practicum.explorewithme.server.entity.Location;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EventMapper {

    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;

    // Преобразование LocationDto в сущность Location
    private Location convertToEntityLocation(LocationDto dto) {
        if (dto == null) {
            return null;
        }
        return Location.builder()
                .lat(dto.getLat())
                .lon(dto.getLon())
                .build();
    }

    // Преобразование сущности Location в LocationDto
    private LocationDto convertToDtoLocation(Location entity) {
        if (entity == null) {
            return null;
        }
        return LocationDto.builder()
                .lat(entity.getLat())
                .lon(entity.getLon())
                .build();
    }

    // Преобразование Event в EventFullDto со статистикой
    public EventFullDto eventFullDto(Event event,
                                     Long confirmedRequests,
                                     Long views) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryMapper.toDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(userMapper.toShortDto(event.getInitiator()))
                .location(convertToDtoLocation(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState().name())
                .title(event.getTitle())
                .views(views)
                .build();
    }

    // Преобразование NewEventDto в Event (для создания нового события)
    public Event toEntity(NewEventDto dtoEntity) {
        return Event.builder()
                .title(dtoEntity.getTitle())
                .annotation(dtoEntity.getAnnotation())
                .description(dtoEntity.getDescription())
                .eventDate(dtoEntity.getEventDate())
                .location(convertToEntityLocation(dtoEntity.getLocation()))
                .paid(Optional.ofNullable(dtoEntity.getPaid()).orElse(false))
                .participantLimit(Optional.ofNullable(dtoEntity.getParticipantLimit()).orElse(0))
                .requestModeration(Optional.ofNullable(dtoEntity.getRequestModeration()).orElse(true))
                .state(EventState.PENDING)
                .build();
    }

    // Обновление Event из UpdateEventUserRequest (для обновления пользователем)
    public void updateFromUserRequest(Event event, UpdateEventUserRequest update) {
        if (update == null || event == null) {
            return;
        }

        // Обновляем только не-null поля
        if (update.getTitle() != null) event.setTitle(update.getTitle());
        if (update.getAnnotation() != null) event.setAnnotation(update.getAnnotation());
        if (update.getDescription() != null) event.setDescription(update.getDescription());
        if (update.getEventDate() != null) event.setEventDate(update.getEventDate());
        if (update.getLocation() != null) event.setLocation(convertToEntityLocation(update.getLocation()));
        if (update.getPaid() != null) event.setPaid(update.getPaid());
        if (update.getParticipantLimit() != null) event.setParticipantLimit(update.getParticipantLimit());
        if (update.getRequestModeration() != null) event.setRequestModeration(update.getRequestModeration());

        // Обработка StateAction
        if (update.getStateAction() != null) {
            switch (update.getStateAction()) {
                case SEND_TO_REVIEW:
                    event.setState(EventState.PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setState(EventState.CANCELED);
                    break;
            }
        }
    }

    // Обновление Event из UpdateEventAdminRequest (для обновления администратором)
    public void updateFromAdminRequest(Event event, UpdateEventAdminRequest update) {
        if (update == null || event == null) {
            return;
        }

        // Обновляем только не-null поля
        if (update.getTitle() != null) event.setTitle(update.getTitle());
        if (update.getAnnotation() != null) event.setAnnotation(update.getAnnotation());
        if (update.getDescription() != null) event.setDescription(update.getDescription());
        if (update.getEventDate() != null) event.setEventDate(update.getEventDate());
        if (update.getLocation() != null) event.setLocation(convertToEntityLocation(update.getLocation()));
        if (update.getPaid() != null) event.setPaid(update.getPaid());
        if (update.getParticipantLimit() != null) event.setParticipantLimit(update.getParticipantLimit());
        if (update.getRequestModeration() != null) event.setRequestModeration(update.getRequestModeration());

        // Обработка StateAction
        if (update.getStateAction() != null) {
            switch (update.getStateAction()) {
                case PUBLISH_EVENT:
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(java.time.LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    event.setState(EventState.CANCELED);
                    break;
            }
        }
    }

    // Преобразование Event в EventShortDto со статистикой
    public EventShortDto toShortDto(Event event, Long confirmedRequests, Long views) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryMapper.toDto(event.getCategory()))
                .eventDate(event.getEventDate())
                .initiator(userMapper.toShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .confirmedRequests(confirmedRequests)
                .views(views)
                .build();
    }
}
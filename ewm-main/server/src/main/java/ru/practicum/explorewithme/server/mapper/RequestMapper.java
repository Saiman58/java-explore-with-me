package ru.practicum.explorewithme.server.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.server.dto.request.ParticipationRequestDto;
import ru.practicum.explorewithme.server.entity.ParticipationRequest;

@Component
public class RequestMapper {

    // Преобразование ParticipationRequest в ParticipationRequestDto
    public ParticipationRequestDto toDto(ParticipationRequest request) {
        ParticipationRequestDto dto = new ParticipationRequestDto();
        dto.setId(request.getId());
        dto.setCreated(request.getCreated());
        dto.setEvent(request.getEvent().getId());
        dto.setRequester(request.getRequester().getId());
        dto.setStatus(request.getStatus().name());
        return dto;
    }
}
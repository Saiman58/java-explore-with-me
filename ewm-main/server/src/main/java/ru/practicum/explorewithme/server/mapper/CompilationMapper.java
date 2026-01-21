package ru.practicum.explorewithme.server.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.server.dto.compilation.CompilationDto;
import ru.practicum.explorewithme.server.dto.compilation.NewCompilationDto;
import ru.practicum.explorewithme.server.dto.event.EventShortDto;
import ru.practicum.explorewithme.server.entity.Compilation;
import ru.practicum.explorewithme.server.entity.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CompilationMapper {

    private final EventMapper eventMapper;

    // Преобразование NewCompilationDto в Compilation (для создания)
    public Compilation toEntity(NewCompilationDto dto) {
        return Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned())
                .build();
    }

    // Преобразование Compilation в CompilationDto без статистики
    public CompilationDto toDtoWithoutStats(Compilation compilation) {
        CompilationDto dto = new CompilationDto();
        dto.setId(compilation.getId());
        dto.setTitle(compilation.getTitle());
        dto.setPinned(compilation.getPinned());

        if (compilation.getEvents() != null && !compilation.getEvents().isEmpty()) {
            List<EventShortDto> eventDtos = compilation.getEvents().stream()
                    .map(event -> eventMapper.toShortDto(event, 0L, 0L))
                    .collect(Collectors.toList());
            dto.setEvents(eventDtos);
        } else {
            dto.setEvents(new ArrayList<>());
        }

        return dto;
    }

    // Добавление событий со статистикой к существующему CompilationDto
    public void addEventsWithStats(CompilationDto dto,
                                   List<Event> events,
                                   List<Long> confirmedRequestsList,
                                   List<Long> viewsList) {
        List<EventShortDto> eventDtos = new ArrayList<>();

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            Long confirmedRequests = confirmedRequestsList.get(i);
            Long views = viewsList.get(i);

            EventShortDto eventDto = eventMapper.toShortDto(event, confirmedRequests, views);
            eventDtos.add(eventDto);
        }

        dto.setEvents(eventDtos);
    }
}
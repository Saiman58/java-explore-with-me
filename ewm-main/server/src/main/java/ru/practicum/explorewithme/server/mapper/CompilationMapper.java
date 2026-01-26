package ru.practicum.explorewithme.server.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.compilation.dto.CompilationDto;
import ru.practicum.explorewithme.server.entity.Compilation;
import ru.practicum.explorewithme.event.dto.EventShortDto;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CompilationMapper {

    public CompilationDto toDto(Compilation compilation, List<EventShortDto> events) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .events(events != null ? events : List.of())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }
}
package ru.practicum.explorewithme.compilation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explorewithme.event.dto.EventShortDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompilationDto {

    private List<EventShortDto> events;

    @NotNull(message = "ID подборки не может быть null")
    private Long id;

    private Boolean pinned;

    @Size(max = 50, message = "Длина заголовка подборки не может превышать 50 символов")
    private String title;
}
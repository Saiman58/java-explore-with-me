package ru.practicum.explorewithme.server.dto.event;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {

    @NotNull(message = "Широта должна быть указана")
    private Float lat;

    @NotNull(message = "Долгота должна быть указана")
    private Float lon;
}

package ru.practicum.explorewithme.server.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explorewithme.server.dto.category.CategoryDto;
import ru.practicum.explorewithme.server.dto.user.UserShortDto;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventShortDto {

    private Long id;
    private String title;
    private String annotation;

    private CategoryDto category;
    private UserShortDto initiator;

    @Future(message = "Дата события должна быть не в прошлом")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private Boolean paid;
    private Long confirmedRequests;
    private Long views;
}

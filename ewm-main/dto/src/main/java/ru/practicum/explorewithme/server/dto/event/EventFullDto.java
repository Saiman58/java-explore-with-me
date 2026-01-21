package ru.practicum.explorewithme.server.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.explorewithme.server.dto.category.CategoryDto;
import ru.practicum.explorewithme.server.dto.user.UserShortDto;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {

    private Long id;

    private String title;
    private String annotation;
    private String description;

    private CategoryDto category;
    private UserShortDto initiator;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdOn;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedOn;

    private LocationDto location;

    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private String state;

    private Long confirmedRequests; // Количество подтверждённых запросов
    private Long views;              // Количество просмотров
}

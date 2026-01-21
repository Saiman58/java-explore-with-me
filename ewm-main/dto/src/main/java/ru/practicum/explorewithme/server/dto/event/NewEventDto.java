package ru.practicum.explorewithme.server.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewEventDto {

    @NotBlank(message = "Заголовок события не может быть пустым")
    @Size(min = 3, max = 120, message = "Заголовок события должен быть от 3 до 120 символов")
    private String title;

    @NotBlank(message = "Аннотация события не может быть пустой")
    @Size(min = 20, max = 2000, message = "Аннотация события должна быть от 20 до 2000 символов")
    private String annotation;

    @NotBlank(message = "Описание события не может быть пустым")
    @Size(min = 20, max = 7000, message = "Описание события должно быть от 20 до 7000 символов")
    private String description;

    @NotNull(message = "Категория события должна быть указана")
    private Long category;

    @NotNull(message = "Дата и время события должны быть указаны")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull(message = "Локация события должна быть указана")
    private LocationDto location;

    private Boolean paid;

    @PositiveOrZero(message = "Лимит участников не может быть отрицательным")
    private Integer participantLimit;

    private Boolean requestModeration;

}

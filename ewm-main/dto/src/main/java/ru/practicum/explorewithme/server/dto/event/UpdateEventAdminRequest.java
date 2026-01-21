package ru.practicum.explorewithme.server.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventAdminRequest { //обновление админом

    @Size(min = 3, max = 120, message = "Заголовок события должен быть от 3 до 120 символов")
    private String title;

    @Size(min = 20, max = 2000, message = "Аннотация события должна быть от 20 до 2000 символов")
    private String annotation;

    @Size(min = 20, max = 7000, message = "Описание события должно быть от 20 до 7000 символов")
    private String description;

    private Long category;

    @Future(message = "Дата события должна быть в будущем")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    private LocationDto location;

    private Boolean paid;

    private Integer participantLimit;

    private Boolean requestModeration;

    private StateAction stateAction;

    public enum StateAction {
        PUBLISH_EVENT,  // Опубликовать событие
        REJECT_EVENT    // Отклонить событие
    }
}

package ru.practicum.explorewithme.server.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequest {  //Заявка на участие в событии

    @NotEmpty(message = "Список идентификаторов запросов не может быть пустым")
    private List<Long> requestIds;

    @NotNull(message = "Статус не может быть null")
    private StatusAction status;

    public enum StatusAction {
        CONFIRMED,
        REJECTED
    }
}

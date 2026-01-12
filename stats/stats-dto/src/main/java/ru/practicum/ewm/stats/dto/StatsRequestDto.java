package ru.practicum.ewm.stats.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.AssertTrue;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsRequestDto {  // для запроса статистики

    @NotNull(message = "Дата начала обязательна для заполнения")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime start;

    @NotNull(message = "Дата окончания обязательна для заполнения")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime end;

    // Список URI для которых нужно выгрузить статистику
    private List<String> uris;
    // Нужно ли учитывать только уникальные посещения (только с уникальным IP)
    private Boolean unique = false;

    @AssertTrue(message = "Дата окончания должна быть позже даты начала")
    public boolean isEndAfterStart() {
        return start == null || end == null || !end.isBefore(start);
    }

}
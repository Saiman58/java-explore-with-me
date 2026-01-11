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

    @NotNull(message = "Дата и время начала диапазона за который нужно выгрузить статистику не может быть пустым")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime start;

    @NotNull(message = "Дата и время конца диапазона за который нужно выгрузить статистику не может быть пустым")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime end;

    //Список uri для которых нужно выгрузить статистику
    private List<String> uris;

    //кнопка Нужно ли учитывать только уникальные посещения (только с уникальным ip)
    @Builder.Default
    private Boolean unique = false;

    @AssertTrue(message = "Дата окончания должна быть позже даты начала")
    public boolean isEndAfterStart() {
        return start == null || end == null || !end.isBefore(start);
    }

    @AssertTrue(message = "Дата окончания не может быть в будущем")
    public boolean isEndNotInFuture() {
        return end == null || !end.isAfter(LocalDateTime.now());
    }
}

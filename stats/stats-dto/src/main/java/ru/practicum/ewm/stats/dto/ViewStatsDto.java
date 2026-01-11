package ru.practicum.ewm.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//результат статистики
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViewStatsDto {
    private String app;    // Идентификатор сервиса
    private String uri;    // Путь запроса
    private Long hits;     // Количество просмотров
}

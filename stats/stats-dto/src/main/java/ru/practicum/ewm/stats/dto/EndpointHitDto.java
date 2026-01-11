package ru.practicum.ewm.stats.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EndpointHitDto {  // сохранение информации о просмотре

    private Long id;

    @NotBlank(message = "Идентификатор сервиса для которого записывается информация не может быть пустым")
    @Size(max = 255, message = "Идентификатор сервиса для которого записывается информация не может превышать 255 символов")
    private String app;

    @NotBlank(message = "URI для которого был осуществлен запрос не может быть пустым")
    @Size(max = 2048, message = "URI не может превышать 2048 символов")
    private String uri;

    @NotBlank(message = "IP-адрес пользователя, осуществившего запрос не может быть пустым")
    @Pattern(
            regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
            message = "Некорректный формат IP-адреса. Пример: 192.168.1.1")
    private String ip;

    @NotNull(message = "Дата и время, когда был совершен запрос к эндпоинту не может быть пустым")
    @PastOrPresent(message = "Дата и время, когда был совершен запрос к эндпоинту должно быть в прошлом или настоящем")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

}

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

    @NotBlank(message = "Идентификатор сервиса не может быть пустым")
    @Size(max = 255, message = "Идентификатор сервиса не может превышать 255 символов")
    private String app;

    @NotBlank(message = "URI не может быть пустым")
    @Size(max = 2048, message = "URI не может превышать 2048 символов")
    private String uri;

    @NotBlank(message = "IP-адрес не может быть пустым")
    @Pattern(
            regexp = "^^((25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.|$)){4}$",
            message = "Неверный формат IP-адреса. Пример: 192.168.1.1"
    )
    private String ip;

    @NotNull(message = "Временная метка не может быть null")
    @PastOrPresent(message = "Временная метка должна быть в прошлом или настоящем")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

}
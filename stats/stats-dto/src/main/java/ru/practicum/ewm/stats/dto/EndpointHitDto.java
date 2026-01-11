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

    @NotBlank(message = "App cannot be blank")
    @Size(max = 255, message = "App identifier cannot exceed 255 characters")
    private String app;

    @NotBlank(message = "URI cannot be blank")
    @Size(max = 2048, message = "URI cannot exceed 2048 characters")
    private String uri;

    @NotBlank(message = "IP cannot be blank")
    @Pattern(
            regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$",
            message = "Invalid IP address"
    )
    private String ip;

    @NotNull(message = "Timestamp cannot be null")
    @PastOrPresent(message = "Timestamp must be in the past or present")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

}

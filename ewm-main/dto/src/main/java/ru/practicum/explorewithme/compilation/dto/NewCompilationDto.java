package ru.practicum.explorewithme.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {

    private List<Long> events;

    private Boolean pinned;

    @NotBlank
    @Size(max = 50, message = "Длина заголовка подборки не может превышать 50 символов")
    private String title;
}
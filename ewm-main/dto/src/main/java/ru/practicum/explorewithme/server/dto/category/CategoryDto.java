package ru.practicum.explorewithme.server.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    private Long id;

    @NotBlank(message = "Название категории не может быть пустым")
    @Size(min = 1, max = 50, message = "Длина названия категории должна быть от 1 до 50 символов")
    private String name;
}

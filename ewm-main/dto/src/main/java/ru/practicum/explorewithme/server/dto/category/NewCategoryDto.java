package ru.practicum.explorewithme.server.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewCategoryDto {

    @NotBlank(message = "Название категории не может быть пустым")
    @Size(min = 1, max = 50, message = "Количество символов должно быть от 1 до 50")
    private String name;
}

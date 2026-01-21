package ru.practicum.explorewithme.server.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewUserRequest {   //создание пользователя

    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 2, max = 250, message = "Количество символов должно быть от 2 до 50")
    private String name;

    @NotBlank
    @Email
    @Size(min = 6, max = 254, message = "Количество символов должно быть от 6 до 254")
    private String email;
}

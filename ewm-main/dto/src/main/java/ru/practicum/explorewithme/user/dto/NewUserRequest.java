package ru.practicum.explorewithme.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequest {

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email must not be blank")
    @Size(min = 6, max = 254, message = "Email length must be between 6 and 254")
    private String email;

    @NotBlank(message = "Name must not be blank")
    @Size(min = 2, max = 250, message = "Name length must be between 2 and 250")
    private String name;
}
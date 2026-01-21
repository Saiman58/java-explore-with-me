package ru.practicum.explorewithme.server.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {   //для ответа

    private String email;

    private Long id;

    private String name;
}

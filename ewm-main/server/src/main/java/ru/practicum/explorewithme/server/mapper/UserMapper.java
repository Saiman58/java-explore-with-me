package ru.practicum.explorewithme.server.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.server.dto.user.NewUserRequest;
import ru.practicum.explorewithme.server.dto.user.UserDto;
import ru.practicum.explorewithme.server.dto.user.UserShortDto;
import ru.practicum.explorewithme.server.entity.User;

@Component
public class UserMapper {

    // Преобразование NewUserRequest в User (для создания)
    public User toEntity(NewUserRequest newUserRequest) {
        return User.builder()
                .name(newUserRequest.getName())
                .email(newUserRequest.getEmail())
                .build();
    }

    // Преобразование User в UserDto (полная информация)
    public UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    // Преобразование User в UserShortDto (краткая информация)
    public UserShortDto toShortDto(User user) {
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}
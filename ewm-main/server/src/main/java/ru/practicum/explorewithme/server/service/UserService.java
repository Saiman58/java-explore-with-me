package ru.practicum.explorewithme.server.service;

import ru.practicum.explorewithme.user.dto.NewUserRequest;
import ru.practicum.explorewithme.user.dto.UserDto;
import ru.practicum.explorewithme.server.entity.User;

import java.util.List;

public interface UserService {

    // Создать пользователя
    UserDto create(NewUserRequest newUser);

    // Получить всех пользователей
    List<UserDto> getAll(List<Long> ids, Integer from, Integer size);

    // Удалить пользователя
    void delete(Long userId);

    // Получить пользователя по ID
    User getById(Long userId);
}
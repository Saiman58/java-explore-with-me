package ru.practicum.explorewithme.server.service;

import ru.practicum.explorewithme.server.dto.user.NewUserRequest;
import ru.practicum.explorewithme.server.dto.user.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(NewUserRequest newUserRequest);

    List<UserDto> getUsers(List<Long> ids, int from, int size);

    void deleteUser(Long userId);
}

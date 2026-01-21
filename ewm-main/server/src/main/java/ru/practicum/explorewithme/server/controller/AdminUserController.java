package ru.practicum.explorewithme.server.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.server.dto.user.NewUserRequest;
import ru.practicum.explorewithme.server.dto.user.UserDto;
import ru.practicum.explorewithme.server.service.UserService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    // POST /admin/users
    // Добавление нового пользователя
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody NewUserRequest newUserRequest) {
        log.info("POST /admin/users: {}", newUserRequest);
        return userService.createUser(newUserRequest);
    }

    // GET /admin/users
    // Получение информации о пользователях
    @GetMapping
    public List<UserDto> getUsers(
            @RequestParam(required = false) List<Long> ids,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size) {
        log.info("GET /admin/users?ids={}&from={}&size={}", ids, from, size);
        return userService.getUsers(ids, from, size);
    }

    // DELETE /admin/users/{userId}
    // Удаление пользователя
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("DELETE /admin/users/{}", userId);
        userService.deleteUser(userId);
    }
}
package ru.practicum.explorewithme.server.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.user.dto.NewUserRequest;
import ru.practicum.explorewithme.user.dto.UserDto;
import ru.practicum.explorewithme.server.service.UserServiceImpl;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Validated
public class AdminUserController {
    private final UserServiceImpl userServiceImpl;

    // GET /admin/users
    // Получение информации о пользователях
    @GetMapping
    public ResponseEntity<List<UserDto>> getAll(@RequestParam(required = false) List<Long> ids,
                                                @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("[AdminUserController] GET /admin/users?ids={}&from={}&size={}", ids, from, size);

        List<UserDto> users = userServiceImpl.getAll(ids, from, size);
        log.debug("[AdminUserController] Найдено {} пользователей", users.size());

        return ResponseEntity.ok(users);
    }

    // POST /admin/users
    // Добавление нового пользователя
    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody NewUserRequest newUser) {
        log.info("[AdminUserController] POST /admin/users: name='{}', email='{}'",
                newUser.getName(), newUser.getEmail());

        UserDto user = userServiceImpl.create(newUser);
        log.info("[AdminUserController] Пользователь создан: id={}, email={}",
                user.getId(), user.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    // DELETE /admin/users/{userId}
    // Удаление пользователя
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long userId) {
        log.info("[AdminUserController] DELETE /admin/users/{}", userId);

        userServiceImpl.delete(userId);
        log.info("[AdminUserController] Пользователь удалён: id={}", userId);
    }
}
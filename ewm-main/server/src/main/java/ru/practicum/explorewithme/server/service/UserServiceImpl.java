package ru.practicum.explorewithme.server.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.user.dto.NewUserRequest;
import ru.practicum.explorewithme.user.dto.UserDto;
import ru.practicum.explorewithme.server.repository.UserRepository;
import ru.practicum.explorewithme.server.entity.User;
import ru.practicum.explorewithme.server.exception.EntityNotFoundException;
import ru.practicum.explorewithme.server.mapper.UserMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String USER_NOT_FOUND = "Пользователь с id=%d не найден";

    @Override
    @Transactional
    public UserDto create(NewUserRequest newUser) {
        log.info("[UserService] Создание пользователя: email={}", newUser.getEmail());

        if (userRepository.existsByEmail(newUser.getEmail())) {
            log.warn("[UserService] Email уже существует: {}", newUser.getEmail());
            throw new IllegalStateException("Email уже существует: " + newUser.getEmail());
        }

        User user = userMapper.toEntity(newUser);
        user = userRepository.save(user);
        entityManager.flush();

        log.info("[UserService] Пользователь создан: id={}, email={}", user.getId(), user.getEmail());
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAll(List<Long> ids, Integer from, Integer size) {
        log.debug("[UserService] Получение пользователей: ids={}, from={}, size={}", ids, from, size);

        PageRequest pageable = PageRequest.of(from / size, size);
        List<User> users;

        if (ids != null && !ids.isEmpty()) {
            users = userRepository.findAllByIdIn(ids, pageable);
            log.debug("[UserService] Найдено {} пользователей по ids", users.size());
        } else {
            Page<User> page = userRepository.findAll(pageable);
            users = page.getContent();
            log.debug("[UserService] Найдено {} пользователей (всего)", users.size());
        }

        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        log.info("[UserService] Удаление пользователя: id={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("[UserService] Пользователь не найден: id={}", userId);
                    return new EntityNotFoundException(String.format(USER_NOT_FOUND, userId));
                });

        boolean hasEvents = entityManager.createQuery(
                        "SELECT COUNT(e) > 0 FROM Event e WHERE e.initiator.id = :userId", Boolean.class)
                .setParameter("userId", userId)
                .getSingleResult();

        if (hasEvents) {
            log.warn("[UserService] Нельзя удалить пользователя со связанными событиями: id={}", userId);
            throw new IllegalStateException("Нельзя удалить пользователя с связанными событиями");
        }

        userRepository.delete(user);
        log.info("[UserService] Пользователь удалён: id={}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public User getById(Long userId) {
        log.debug("[UserService] Получение пользователя: id={}", userId);

        Optional<User> user = userRepository.findById(userId);

        return user.orElseThrow(() -> {
            log.error("[UserService] Пользователь не найден: id={}", userId);
            return new EntityNotFoundException(String.format(USER_NOT_FOUND, userId));
        });
    }
}
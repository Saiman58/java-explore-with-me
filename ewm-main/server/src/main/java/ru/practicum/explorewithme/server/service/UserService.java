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
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @PersistenceContext
    private EntityManager entityManager;

    private static final String USER_NOT_FOUND = "Пользователь с id=%d не найден";

    @Transactional
    public UserDto create(NewUserRequest newUser) {
        log.info("Создание пользователя с email '{}'", newUser.getEmail());
        if (userRepository.existsByEmail(newUser.getEmail())) {
            throw new IllegalStateException("Email уже существует: " + newUser.getEmail());
        }
        User user = userMapper.toEntity(newUser);
        user = userRepository.save(user);
        entityManager.flush();
        log.info("Пользователь создан с ID {}", user.getId());
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAll(List<Long> ids, Integer from, Integer size) {
        log.debug("Получение пользователей ids={} from {} size {}", ids, from, size);
        PageRequest pageable = PageRequest.of(from / size, size);
        List<User> users;
        if (ids != null && !ids.isEmpty()) {
            users = userRepository.findAllByIdIn(ids, pageable);
        } else {
            Page<User> page = userRepository.findAll(pageable);
            users = page.getContent();
        }
        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long userId) {
        log.info("Удаление пользователя ID {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format(USER_NOT_FOUND, userId)));

        boolean hasEvents = entityManager.createQuery(
                        "SELECT COUNT(e) > 0 FROM Event e WHERE e.initiator.id = :userId", Boolean.class)
                .setParameter("userId", userId)
                .getSingleResult();

        if (hasEvents) {
            log.warn("Нельзя удалить пользователя ID {} — есть связанные события", userId);
            throw new IllegalStateException("Нельзя удалить пользователя с связанными событиями");
        }

        userRepository.delete(user);
        log.info("Пользователь ID {} удален", userId);
    }

    @Transactional(readOnly = true)
    public User getById(Long userId) {
        log.debug("Получение пользователя ID {}", userId);
        Optional<User> user = userRepository.findById(userId);
        return user.orElseThrow(() -> new EntityNotFoundException(String.format(USER_NOT_FOUND, userId)));
    }
}
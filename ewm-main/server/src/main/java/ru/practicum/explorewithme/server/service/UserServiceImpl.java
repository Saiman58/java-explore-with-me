package ru.practicum.explorewithme.server.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.explorewithme.server.dto.user.NewUserRequest;
import ru.practicum.explorewithme.server.dto.user.UserDto;
import ru.practicum.explorewithme.server.entity.User;
import ru.practicum.explorewithme.server.exception.ConflictException;
import ru.practicum.explorewithme.server.exception.NotFoundException;
import ru.practicum.explorewithme.server.mapper.UserMapper;
import ru.practicum.explorewithme.server.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        log.info("Создание пользователя: {}", newUserRequest.getEmail());

        // Проверка уникальности email
        if (userRepository.existsByEmail(newUserRequest.getEmail())) {
            throw new ConflictException("Пользователь с email=" + newUserRequest.getEmail() + " уже существует");
        }

        User user = userMapper.toEntity(newUserRequest);
        User savedUser = userRepository.save(user);

        log.info("Пользователь создан с id={}", savedUser.getId());

        return userMapper.toDto(savedUser);
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        log.info("Получение пользователей: ids={}, from={}, size={}", ids, from, size);

        // Вычисляем номер страницы
        int pageNumber = from / size;
        Pageable pageable = PageRequest.of(pageNumber, size);

        List<User> users;
        if (ids == null || ids.isEmpty()) {
            // Если ids не указаны - возвращаем всех с пагинацией
            users = userRepository.findAll(pageable).getContent();
        } else {
            // Если ids указаны - возвращаем только указанных
            users = userRepository.findByIdIn(ids, pageable);
        }

        return users.stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя с id={}", userId);

        //проверка существование пользователя
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        userRepository.deleteById(userId);
        userRepository.flush(); //для проверок с бд H2

        log.info("Пользователь с id={} удален", userId);
    }
}

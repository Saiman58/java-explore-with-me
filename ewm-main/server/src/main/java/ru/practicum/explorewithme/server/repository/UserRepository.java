package ru.practicum.explorewithme.server.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.server.entity.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    // Поиск пользователей по списку ID с пагинацией
    List<User> findByIdIn(List<Long> ids, Pageable pageable);

    // Проверка существования пользователя с email
    boolean existsByEmail(String email);
}
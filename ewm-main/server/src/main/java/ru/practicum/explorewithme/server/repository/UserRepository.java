package ru.practicum.explorewithme.server.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.server.entity.User;

import java.util.List;

// Репозиторий пользователей
public interface UserRepository extends JpaRepository<User, Long> {

    // Проверка существования пользователя по email
    boolean existsByEmail(String email);

    // Получить пользователей по списку ID с пагинацией
    List<User> findAllByIdIn(List<Long> ids, Pageable pageable);

    // Проверка существования пользователя по ID
    boolean existsById(Long id);
}
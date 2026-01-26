package ru.practicum.explorewithme.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.server.entity.Category;

// Репозиторий категорий
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Проверка существования категории по имени
    boolean existsByName(String name);

    // Проверка существования категории по имени, исключая указанный ID
    boolean existsByNameAndIdNot(String name, Long id);
}
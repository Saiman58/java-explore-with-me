package ru.practicum.explorewithme.server.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.server.entity.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Проверка существования категории с указанным именем
    boolean existsByName(String name);

    // Получение всех категорий с пагинацией
    List<Category> findAllBy(Pageable pageable);
}
package ru.practicum.explorewithme.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.server.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
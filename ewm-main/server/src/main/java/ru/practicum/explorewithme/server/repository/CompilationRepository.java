package ru.practicum.explorewithme.server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.server.entity.Compilation;

// Репозиторий подборок
public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    // Проверка существования подборки по названию
    boolean existsByTitle(String title);

    // Проверка существования подборки по названию, исключая указанный ID
    boolean existsByTitleAndIdNot(String title, Long id);

    // Проверка существования подборки по ID
    boolean existsById(Long id);

    // Поиск подборок по статусу закрепления с пагинацией
    Page<Compilation> findAllByPinned(boolean pinned, Pageable pageable);
}
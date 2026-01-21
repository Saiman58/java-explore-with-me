package ru.practicum.explorewithme.server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.server.entity.Compilation;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    //Поиск подборок с фильтром по закреплению (pinned)
    @Query("SELECT c FROM Compilation c " +
            "WHERE (:pinned IS NULL OR c.pinned = :pinned)")
    Page<Compilation> findAllByPinned(@Param("pinned") Boolean pinned, Pageable pageable);

    //Проверка существования подборки по названию
    boolean existsByTitle(String title);
}

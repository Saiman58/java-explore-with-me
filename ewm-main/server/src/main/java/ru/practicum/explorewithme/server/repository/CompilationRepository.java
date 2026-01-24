package ru.practicum.explorewithme.server.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.server.entity.Compilation;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    boolean existsByTitle(String title);

    boolean existsByTitleAndIdNot(String title, Long id);

    boolean existsById(Long id);

    Page<Compilation> findAllByPinned(boolean pinned, Pageable pageable);
}
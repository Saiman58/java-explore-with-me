package ru.practicum.explorewithme.stats.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.stats.dto.ViewStats;
import ru.practicum.explorewithme.stats.server.entity.Hit;

import java.time.LocalDateTime;
import java.util.List;

// Репозиторий для работы со статистикой
public interface HitRepository extends JpaRepository<Hit, Long> {

    // Получить полную статистику (все хиты)
    @Query("SELECT new ru.practicum.explorewithme.stats.dto.ViewStats(h.app, h.uri, COUNT(h.id)) " +
            "FROM Hit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR h.uri IN :uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h.id) DESC")
    List<ViewStats> findStats(@Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end,
                              @Param("uris") List<String> uris);

    // Получить уникальную статистику (уникальные IP)
    @Query("SELECT new ru.practicum.explorewithme.stats.dto.ViewStats(h.app, h.uri, COUNT(DISTINCT h.ip)) " +
            "FROM Hit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR h.uri IN :uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<ViewStats> findUniqueStats(@Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end,
                                    @Param("uris") List<String> uris);

    // Найти хиты по URI в период времени
    List<Hit> findAllByUriAndTimestampBetween(String uri, LocalDateTime start, LocalDateTime end);

    // Найти все хиты в период времени
    List<Hit> findAllByTimestampBetween(LocalDateTime start, LocalDateTime end);

    // Найти уникальные хиты по URI, APP и IP в период времени
    @Query("SELECT h FROM Hit h WHERE h.timestamp BETWEEN :start AND :end GROUP BY h.uri, h.app, h.ip")
    List<Hit> findDistinctByTimestampBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
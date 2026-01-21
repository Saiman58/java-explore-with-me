package ru.practicum.explorewithme.stats.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.stats.dto.ViewStats;
import ru.practicum.explorewithme.stats.server.entity.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface HitRepository extends JpaRepository<Hit, Long> {

    // Получение статистики (все хиты)
    @Query("SELECT new ru.practicum.explorewithme.stats.dto.ViewStats(" +
            "h.app, h.uri, " +
            "COUNT(h.id)) " +
            "FROM Hit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR h.uri IN :uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h.id) DESC")
    List<ViewStats> findStats(@Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end,
                              @Param("uris") List<String> uris);

    // Получение уникальной статистики (по уникальным IP)
    @Query("SELECT new ru.practicum.explorewithme.stats.dto.ViewStats(" +
            "h.app, h.uri, COUNT(DISTINCT h.ip)) " +
            "FROM Hit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR h.uri IN :uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<ViewStats> findUniqueStats(@Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end,
                                    @Param("uris") List<String> uris);
}
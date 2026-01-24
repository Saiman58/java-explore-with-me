package ru.practicum.explorewithme.stats.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.stats.dto.ViewStats;
import ru.practicum.explorewithme.stats.server.entity.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface HitRepository extends JpaRepository<Hit, Long> {

    @Query("SELECT new ru.practicum.explorewithme.stats.dto.ViewStats(h.app, h.uri, COUNT(h.id)) " +
            "FROM Hit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR h.uri IN :uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h.id) DESC")
    List<ViewStats> findStats(@Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end,
                              @Param("uris") List<String> uris);

    @Query("SELECT new ru.practicum.explorewithme.stats.dto.ViewStats(h.app, h.uri, COUNT(DISTINCT h.ip)) " +
            "FROM Hit h " +
            "WHERE h.timestamp BETWEEN :start AND :end " +
            "AND (:uris IS NULL OR h.uri IN :uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<ViewStats> findUniqueStats(@Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end,
                                    @Param("uris") List<String> uris);

    List<Hit> findAllByUriAndTimestampBetween(String uri, LocalDateTime start, LocalDateTime end);

    List<Hit> findAllByTimestampBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT h FROM Hit h WHERE h.timestamp BETWEEN :start AND :end GROUP BY h.uri, h.app, h.ip")
    List<Hit> findDistinctByTimestampBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
package ru.practicum.explorewithme.stats.server.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.explorewithme.stats.server.entity.Hit;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class HitRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private HitRepository repository;

    @Test
    void findAllByUriAndTimestampBetween() {

        Hit hit1 = new Hit(null, "app", "/events/1", "ip", LocalDateTime.of(2025, 12, 27, 12, 0));
        entityManager.persistAndFlush(hit1);

        Hit hit2 = new Hit(null, "app", "/events/2", "ip", LocalDateTime.of(2025, 12, 27, 13, 0));
        entityManager.persistAndFlush(hit2);

        List<Hit> hits = repository.findAllByUriAndTimestampBetween("/events/1", LocalDateTime.of(2025, 12, 27, 11, 0), LocalDateTime.of(2025, 12, 27, 13, 0));

        assertEquals(1, hits.size());
        assertEquals("/events/1", hits.get(0).getUri());
    }
}
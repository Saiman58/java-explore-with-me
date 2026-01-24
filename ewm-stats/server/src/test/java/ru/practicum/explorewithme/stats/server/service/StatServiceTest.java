package ru.practicum.explorewithme.stats.server.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.explorewithme.stats.dto.ViewStats;
import ru.practicum.explorewithme.stats.server.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatServiceTest {
    @Mock
    private HitRepository hitRepository;

    @InjectMocks
    private StatService statService;

    @Test
    void getStats_nonUnique() {

        LocalDateTime start = LocalDateTime.of(2025, 12, 27, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 12, 27, 18, 0);
        List<String> uris = List.of("/events/1");
        List<ViewStats> mockStats = List.of(new ViewStats("app", "/events/1", 5L));
        when(hitRepository.findStats(any(), any(), any())).thenReturn(mockStats);

        List<ViewStats> stats = statService.getStats(start, end, uris, false);

        assertEquals(1, stats.size());
        assertEquals("/events/1", stats.get(0).getUri());
        assertEquals(5L, stats.get(0).getHits());
    }

    @Test
    void getStats_unique() {

        LocalDateTime start = LocalDateTime.of(2025, 12, 27, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 12, 27, 18, 0);
        List<String> uris = List.of("/events/1");
        List<ViewStats> mockStats = List.of(new ViewStats("app", "/events/1", 3L));
        when(hitRepository.findUniqueStats(any(), any(), any())).thenReturn(mockStats);

        List<ViewStats> stats = statService.getStats(start, end, uris, true);

        assertEquals(1, stats.size());
        assertEquals("/events/1", stats.get(0).getUri());
        assertEquals(3L, stats.get(0).getHits());
    }
}
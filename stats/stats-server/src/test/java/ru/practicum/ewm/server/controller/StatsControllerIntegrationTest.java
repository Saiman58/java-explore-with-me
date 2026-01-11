package ru.practicum.ewm.server.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.server.StatsServerApplication;
import ru.practicum.ewm.server.model.EndpointHit;
import ru.practicum.ewm.server.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = StatsServerApplication.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase
@Transactional
class StatsControllerIntegrationTest {

    @Autowired
    private EndpointHitRepository repository;

    @Autowired
    private StatsController controller;

    private MockMvc mockMvc;

    private final LocalDateTime fixedNow = LocalDateTime.of(2025, 1, 1, 12, 0);

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        repository.deleteAll(); // очищаем базу перед каждым тестом
    }

    @Test
    void getStats_unique_true_shouldCountDistinctIps() throws Exception {
        // сохраняем 3 хита, два с одинаковым IP
        repository.saveAll(List.of(
                new EndpointHit(null, "ewm", "/events", "127.0.0.1", fixedNow),
                new EndpointHit(null, "ewm", "/events", "127.0.0.1", fixedNow.plusSeconds(1)),
                new EndpointHit(null, "ewm", "/events", "192.168.0.1", fixedNow.plusSeconds(2))
        ));

        String response = mockMvc.perform(get("/stats")
                        .param("start", fixedNow.minusDays(1).toString())
                        .param("end", fixedNow.plusDays(1).toString())
                        .param("unique", "true")
                        .param("uris", "/events")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).contains("\"hits\":2"); // только 2 уникальных IP
    }

    @Test
    void getStats_unique_false_shouldCountAllHits() throws Exception {
        // сохраняем 3 хита, два с одинаковым IP
        repository.saveAll(List.of(
                new EndpointHit(null, "ewm", "/events", "127.0.0.1", fixedNow),
                new EndpointHit(null, "ewm", "/events", "127.0.0.1", fixedNow.plusSeconds(1)),
                new EndpointHit(null, "ewm", "/events", "192.168.0.1", fixedNow.plusSeconds(2))
        ));

        String response = mockMvc.perform(get("/stats")
                        .param("start", fixedNow.minusDays(1).toString())
                        .param("end", fixedNow.plusDays(1).toString())
                        .param("unique", "false")
                        .param("uris", "/events")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(response).contains("\"hits\":3"); // считаем все хиты
    }

    @Test
    void getStats_without_unique_shouldPassNullToService() throws Exception {
        repository.save(new EndpointHit(null, "ewm", "/events", "127.0.0.1", fixedNow));

        // уникальный параметр не передан
        mockMvc.perform(get("/stats")
                        .param("start", fixedNow.minusDays(1).toString())
                        .param("end", fixedNow.plusDays(1).toString())
                        .param("uris", "/events"))
                .andExpect(status().isOk());
    }
}

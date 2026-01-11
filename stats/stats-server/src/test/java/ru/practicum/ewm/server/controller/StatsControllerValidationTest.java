package ru.practicum.ewm.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.server.StatsServerApplication;
import ru.practicum.ewm.server.model.EndpointHit;
import ru.practicum.ewm.server.service.StatsService;
import ru.practicum.ewm.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = StatsServerApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class StatsControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private StatsService service;

    private String json(Object body) throws Exception {
        return mapper.writeValueAsString(body);
    }

    // ------------------------ /hit ------------------------
    @Test
    void saveHit_whenAppValid_shouldReturn201() throws Exception {
        var hit = new EndpointHit(null, "ewm-service", "/events", "127.0.0.1", LocalDateTime.now());

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(hit)))
                .andExpect(status().isCreated());
    }

    @Test
    void saveHit_whenAppBlank_shouldReturnErrorMessage() throws Exception {
        var hit = new EndpointHit(null, "", "/events", "127.0.0.1", LocalDateTime.now());

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(hit)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.app").value("App cannot be blank"));
    }

    @Test
    void saveHit_whenUriBlank_shouldReturnErrorMessage() throws Exception {
        var hit = new EndpointHit(null, "ewm", "", "127.0.0.1", LocalDateTime.now());

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(hit)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.uri").value("URI cannot be blank"));
    }

    @Test
    void saveHit_whenIpInvalid_shouldReturnValidationMessage() throws Exception {
        var hit = new EndpointHit(null, "ewm", "/events", "bad-ip", LocalDateTime.now());

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(hit)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.ip").value("Invalid IP address"));
    }

    @Test
    void saveHit_whenTimestampInFuture_shouldReturnMessage() throws Exception {
        var hit = new EndpointHit(null, "ewm", "/events", "127.0.0.1", LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(hit)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.timestamp").value("Timestamp must be in the past or present"));
    }

    // ------------------------ /stats ------------------------
    @Test
    void getStats_whenEndBeforeStart_shouldReturnValidationError() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("start", "2025-02-01 00:00:00")
                        .param("end", "2025-01-01 00:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.end").value("End date must be after start date"));
    }

    @Test
    void getStats_whenInvalidDateFormat_shouldReturnValidationError() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("start", "2025/01/01 00:00")
                        .param("end", "2025/02/01 00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    void getStats_whenStartNull_shouldReturnValidationError() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("end", "2025-02-01T00:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.start").value("Start date is required"));
    }

    @Test
    void getStats_whenEndNull_shouldReturnValidationError() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("start", "2025-01-01T00:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.end").value("End date is required"));
    }

    // ------------------------ Положительные сценарии ------------------------
    @Test
    void getStats_whenValidDatesAndNoUris_shouldReturnOk() throws Exception {
        when(service.getStats(any(), any(), any(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/stats")
                        .param("start", "2025-01-01T00:00:00")
                        .param("end", "2025-01-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getStats_whenValidDatesAndUris_shouldReturnStats() throws Exception {
        var stat = new ViewStatsDto("ewm", "/events", 10L);
        when(service.getStats(any(), any(), any(), any())).thenReturn(List.of(stat));

        mockMvc.perform(get("/stats")
                        .param("start", "2025-01-01T00:00:00")
                        .param("end", "2025-01-31T23:59:59")
                        .param("unique", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app").value("ewm"))
                .andExpect(jsonPath("$[0].uri").value("/events"))
                .andExpect(jsonPath("$[0].hits").value(10));
    }

    @Test
    void getStats_whenUniqueTrue_shouldPassUniqueParamTrueToService() throws Exception {
        when(service.getStats(any(), any(), any(), any()))
                .thenReturn(List.of(new ViewStatsDto("ewm", "/events", 1L)));

        mockMvc.perform(get("/stats")
                        .param("start", "2025-01-01T00:00:00")
                        .param("end", "2025-01-31T23:59:59")
                        .param("unique", "true"))
                .andExpect(status().isOk());

        ArgumentCaptor<Boolean> captor = ArgumentCaptor.forClass(Boolean.class);

        verify(service).getStats(any(), any(), any(), captor.capture());

        assertTrue(captor.getValue());
    }

    @Test
    void getStats_whenUniqueNotProvided_shouldPassNullToService() throws Exception {
        when(service.getStats(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/stats")
                        .param("start", "2025-01-01T00:00:00")
                        .param("end", "2025-01-31T23:59:59"))
                .andExpect(status().isOk());

        ArgumentCaptor<Boolean> captor = ArgumentCaptor.forClass(Boolean.class);

        verify(service).getStats(any(), any(), any(), captor.capture());

        assertFalse(captor.getValue());
    }
}

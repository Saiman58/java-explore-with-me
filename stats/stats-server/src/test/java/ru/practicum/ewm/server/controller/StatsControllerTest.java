package ru.practicum.ewm.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.server.service.StatsService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatsController.class)
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private StatsService service;

    @Test
    void testGetStats_EndBeforeStart() throws Exception {
        mockMvc.perform(get("/stats")
                        .param("start", "2025-02-01T00:00:00")
                        .param("end", "2025-01-01T00:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.end").value("End date must be after start date"));
    }


    @Test
    void testGetStats_Valid() throws Exception {
        when(service.getStats(any(), any(), any(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/stats")
                        .param("start", "2025-01-01T00:00:00")
                        .param("end", "2025-01-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}

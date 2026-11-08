package com.tick_tracker.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "tick.ingest.on-startup=false")
@AutoConfigureMockMvc
class ErrorResponsesTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void validSearchReturns200() throws Exception {
        mockMvc.perform(get("/sightings").param("from", "2022-01-01").param("to", "2022-12-31")).andExpect(status().isOk());
    }

    @Test
    void badDateReturns400() throws Exception {
        mockMvc.perform(get("/sightings").param("from", "banana")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Invalid value 'banana' for 'from'. Dates must look like 2022-01-31."));
    }

    @Test
    void fromAfterToReturns400() throws Exception {
        mockMvc.perform(get("/sightings").param("from", "2023-01-01").param("to", "2022-01-01")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("'from' must be on or before 'to'"));
    }

    @Test
    void unknownPeriodReturns400() throws Exception {
        mockMvc.perform(get("/stats/trends").param("period", "yearly")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("period must be 'weekly' or 'monthly'"));
    }

    @Test
    void unknownEndpointReturns404() throws Exception {
        mockMvc.perform(get("/does-not-exist")).andExpect(status().isNotFound());
    }
}
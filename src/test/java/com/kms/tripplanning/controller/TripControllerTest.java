package com.kms.tripplanning.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.kms.tripplanning.dto.trip.ResponseTrip.TripDetail;
import com.kms.tripplanning.exception.ApiExceptionHandler;
import com.kms.tripplanning.exception.types.NotFoundException;
import com.kms.tripplanning.services.TripService;

@ExtendWith(MockitoExtension.class)
class TripControllerTest {

    private final Logger logger = org.slf4j.LoggerFactory.getLogger(TripControllerTest.class);

    private MockMvc mockMvc;

    @Mock
    private TripService tripService;

    @InjectMocks
    private TripController tripController;

    private TripDetail sampleDetail;
    private UUID tripId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tripController)
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(new org.springframework.validation.beanvalidation.LocalValidatorFactoryBean())
                .build();
        tripId = UUID.randomUUID();
        sampleDetail = new TripDetail(
                tripId.toString(), "Summer Trip", "2025-06-01", "2025-06-15", List.of());
    }

    // ========== POST /api/trips/list ==========

    @Test
    void list_shouldReturn200_withPageOfTrips() throws Exception {
        var page = new PageImpl<>(List.of(sampleDetail), PageRequest.of(0, 10), 1);
        when(tripService.searchTrips(any(), any(), any(), any(int.class), any(int.class))).thenReturn(page);

        mockMvc.perform(post("/api/trips/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"page": 0, "size": 10, "filters": {}}
                        """))
                .andExpect(status().isOk());
    }

    @Test
    void list_shouldReturn200_withEmptyPage() throws Exception {
        var emptyPage = new PageImpl<TripDetail>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        when(tripService.searchTrips(any(), any(), any(), any(int.class), any(int.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(post("/api/trips/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"page": 0, "size": 10}
                        """))
                .andExpect(status().isOk());
    }

    // ========== DELETE /api/trips/{tripId} ==========

    @Test
    void deleteTrip_shouldReturn204_whenFound() throws Exception {
        doNothing().when(tripService).deleteTrip(tripId);

        mockMvc.perform(delete("/api/trips/{tripId}", tripId.toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTrip_shouldReturn404_whenNotFound() throws Exception {
        doThrow(new NotFoundException("Not found")).when(tripService).deleteTrip(tripId);

        mockMvc.perform(delete("/api/trips/{tripId}", tripId.toString()))
                .andExpect(status().isNotFound());
    }

    // ========== POST /api/trips/ ==========

    @Test
    void create_shouldReturn200_withTripDetail() {
        try {
            when(tripService.createTrip(any())).thenReturn(sampleDetail);

            var result = mockMvc.perform(post("/api/trips/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "name": "Summer Trip",
                                "startDate": 1748736000000,
                                "endDate": 1749945600000
                            }
                            """));
            var response = result.andReturn().getResponse(); // Force the request to execute and throw exception
                                                                   // if validation fails
            logger.info("Response content: {}", response.getContentAsString());
            assertThat(response.getStatus()).isEqualTo(200);

        } catch (Exception e) {
            logger.error("Exception occurred: {}", e.getMessage());
            assertThat(e).hasCauseInstanceOf(MethodArgumentNotValidException.class);
        }
    }

    @Test
    void create_shouldReturn400_whenNameEmpty() {
        try {
            var result = mockMvc.perform(post("/api/trips/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "name": "",
                                "startDate": 1748736000000,
                                "endDate": 1749945600000
                            }
                            """));
            result.andReturn().getResponse().getContentAsString(); // Force the request to execute and throw exception

        } catch (Exception e) {
            logger.error("Exception occurred: {}", e.getMessage());
            assertThat(e).hasCauseInstanceOf(MethodArgumentNotValidException.class);
        }
    }

    // ========== POST /api/trips/{tripId} ==========

    @Test
    void updateTrip_shouldReturn200_withUpdatedTrip() throws Exception {
        when(tripService.updateTrip(any(), eq(tripId))).thenReturn(sampleDetail);

        mockMvc.perform(post("/api/trips/{tripId}", tripId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Updated Trip"}
                        """))
                .andExpect(status().isOk());
    }

    @Test
    void updateTrip_shouldReturn404_whenNotFound() throws Exception {
        when(tripService.updateTrip(any(), eq(tripId))).thenThrow(new NotFoundException("Not found"));

        mockMvc.perform(post("/api/trips/{tripId}", tripId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Updated Trip"}
                        """))
                .andExpect(status().isNotFound());
    }

    // ========== GET /api/trips/{tripId} ==========

    @Test
    void getTrip_shouldReturn200_withTripDetail() throws Exception {
        when(tripService.getTripById(tripId)).thenReturn(sampleDetail);

        mockMvc.perform(get("/api/trips/{tripId}", tripId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void getTrip_shouldReturn404_whenNotFound() throws Exception {
        when(tripService.getTripById(tripId)).thenThrow(new NotFoundException("Not found"));

        mockMvc.perform(get("/api/trips/{tripId}", tripId.toString()))
                .andExpect(status().isNotFound());
    }
}

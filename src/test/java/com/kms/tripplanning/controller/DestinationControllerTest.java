package com.kms.tripplanning.controller;

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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.kms.tripplanning.dto.destination.ResponseDestination.DestinationDetail;
import com.kms.tripplanning.exception.ApiExceptionHandler;
import com.kms.tripplanning.exception.types.NotFoundException;
import com.kms.tripplanning.services.DestinationService;

@ExtendWith(MockitoExtension.class)
class DestinationControllerTest {

        private MockMvc mockMvc;

        @Mock
        private DestinationService destinationService;

        @InjectMocks
        private DestinationController destinationController;

        private DestinationDetail sampleDetail;
        private UUID destinationId;

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.standaloneSetup(destinationController)
                                .setControllerAdvice(new ApiExceptionHandler())
                                .build();
                destinationId = UUID.randomUUID();
                sampleDetail = new DestinationDetail();
                sampleDetail.setId(destinationId.toString());
                sampleDetail.setName("Vung Tau");
                sampleDetail.setCity("Vung Tau");
                sampleDetail.setCountry("Vietnam");
                sampleDetail.setRating(4.5f);
                sampleDetail.setLatitude(10.35);
                sampleDetail.setLongitude(107.08);
                sampleDetail.setThumbnailUrl("https://img.com/vt.jpg");
                sampleDetail.setCategories(List.of());
        }

        // ========== POST /api/destinations/list ==========

        @Test
        void list_shouldReturn200_withPageOfDestinations() throws Exception {
                var page = new PageImpl<>(List.of(sampleDetail), PageRequest.of(0, 10), 1);
                when(destinationService.listDestination(any(), any(), any(), any(int.class), any(int.class)))
                                .thenReturn(page);

                mockMvc.perform(post("/api/destinations/list")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"page": 0, "size": 10, "filters": {}}
                                                """))
                                .andExpect(status().isOk());
        }

        @Test
        void list_shouldReturn200_withEmptyPage() throws Exception {
                var emptyPage = new PageImpl<DestinationDetail>(Collections.emptyList(), PageRequest.of(0, 10), 0);
                when(destinationService.listDestination(any(), any(), any(), any(int.class), any(int.class)))
                                .thenReturn(emptyPage);

                mockMvc.perform(post("/api/destinations/list")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"page": 0, "size": 10}
                                                """))
                                .andExpect(status().isOk());
        }

        // ========== POST /api/destinations/{destinationId} ==========

        @Test
        void update_shouldReturn200_withUpdatedDestination() throws Exception {
                when(destinationService.updateDestination(eq(destinationId), any())).thenReturn(sampleDetail);

                mockMvc.perform(post("/api/destinations/{destinationId}", destinationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"name": "Updated"}
                                                """))
                                .andExpect(status().isOk());
        }

        @Test
        void update_shouldReturn404_whenNotFound() throws Exception {
                when(destinationService.updateDestination(eq(destinationId), any()))
                                .thenThrow(new NotFoundException("Not found"));

                mockMvc.perform(post("/api/destinations/{destinationId}", destinationId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"name": "Updated"}
                                                """))
                                .andExpect(status().isNotFound());
        }

        // ========== POST /api/destinations/ ==========

        @Test
        void create_shouldReturn200_withDestinationDetail() throws Exception {
                when(destinationService.createDestination(any())).thenReturn(sampleDetail);

                mockMvc.perform(post("/api/destinations/")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                    "name": "Vung Tau",
                                                    "city": "Vung Tau",
                                                    "country": "Vietnam",
                                                    "latitude": 10.35,
                                                    "longitude": 107.08,
                                                    "rating": 4.5
                                                }
                                                """))
                                .andExpect(status().isOk());
        }

        // ========== DELETE /api/destinations/{destinationId} ==========

        @Test
        void delete_shouldReturn200_whenFound() throws Exception {
                doNothing().when(destinationService).deleteDestination(destinationId);

                mockMvc.perform(delete("/api/destinations/{destinationId}", destinationId))
                                .andExpect(status().isOk());
        }

        @Test
        void delete_shouldReturn404_whenNotFound() throws Exception {
                doThrow(new NotFoundException("Not found")).when(destinationService).deleteDestination(destinationId);

                mockMvc.perform(delete("/api/destinations/{destinationId}", destinationId))
                                .andExpect(status().isNotFound());
        }

        // ========== GET /api/destinations/{destinationId} ==========

        @Test
        void getById_shouldReturn200_withDestinationDetail() throws Exception {
                when(destinationService.getDestinationById(destinationId)).thenReturn(sampleDetail);

                mockMvc.perform(get("/api/destinations/{destinationId}", destinationId))
                                .andExpect(status().isOk());
        }

        @Test
        void getById_shouldReturn404_whenNotFound() throws Exception {
                when(destinationService.getDestinationById(destinationId))
                                .thenThrow(new NotFoundException("Not found"));

                mockMvc.perform(get("/api/destinations/{destinationId}", destinationId))
                                .andExpect(status().isNotFound());
        }
}

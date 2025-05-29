package com.example.demo.controller;

import com.example.demo.entity.Building;
import com.example.demo.entity.Floor;
import com.example.demo.entity.Seat;
import com.example.demo.service.BuildingService;
import com.example.demo.service.SeatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

public class SeatControllerTest {

    @Mock
    private SeatService seatService;

    @Mock
    private BuildingService buildingService;

    @InjectMocks
    private SeatController seatController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(seatController).build();
    }

    @Test
    void getSeatsByBuildingAndFloor_ShouldReturnListOfSeats() throws Exception {
        // Given
        Floor floor = new Floor();
        floor.setId(1L);
        
        List<Seat> seats = Arrays.asList(
            new Seat(),
            new Seat()
        );

        when(seatService.getSeatsByBuildingAndFloor(anyLong(), anyInt())).thenReturn(seats);

        // When & Then
        mockMvc.perform(get("/api/seats/building/1/floor/1/seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getSeatsByBuildingAndFloor_WhenFloorNotFound_ShouldReturnBadRequest() throws Exception {
        // Given
        String errorMessage = "Floor not found in the specified building";
        when(seatService.getSeatsByBuildingAndFloor(anyLong(), anyInt()))
                .thenThrow(new IllegalArgumentException(errorMessage));

        // When & Then
        mockMvc.perform(get("/api/seats/building/1/floor/1/seats"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(errorMessage));
    }

    @Test
    void getAllBuildings_ShouldReturnListOfBuildings() throws Exception {
        // Given
        List<Building> buildings = Arrays.asList(
            new Building(),
            new Building()
        );

        when(buildingService.getAllBuildings()).thenReturn(buildings);

        // When & Then
        mockMvc.perform(get("/api/seats/buildings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }
} 
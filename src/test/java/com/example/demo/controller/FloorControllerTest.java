package com.example.demo.controller;

import com.example.demo.entity.Floor;
import com.example.demo.service.FloorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FloorControllerTest {

    @Mock
    private FloorService floorService;

    @InjectMocks
    private FloorController floorController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(floorController).build();
    }

    @Test
    void getFloorsByBuilding_ShouldReturnListOfFloors() throws Exception {
        List<Floor> floors = Arrays.asList(
            new Floor(),
            new Floor()
        );

        when(floorService.getFloorsByBuilding(1L)).thenReturn(floors);

        mockMvc.perform(get("/api/floors/building/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }
} 
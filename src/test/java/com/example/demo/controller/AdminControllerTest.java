package com.example.demo.controller;

import com.example.demo.entity.Seat;
import com.example.demo.service.SeatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AdminControllerTest {

    @Mock
    private SeatService seatService;

    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .build();
    }

    @Test
    void forceReturnSeat_ShouldReturnSuccessMessage() throws Exception {
        doNothing().when(seatService).forceReturnSeat(1L);

        mockMvc.perform(post("/api/admin/force-return/1")
                .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().string("Seat force returned successfully"));
    }

    @Test
    void getDetailedSeatStatus_ShouldReturnListOfSeats() throws Exception {
        List<Seat> seats = Arrays.asList(
            new Seat(),
            new Seat()
        );

        when(seatService.getSeatsByBuildingAndFloor(1L, 1)).thenReturn(seats);

        mockMvc.perform(get("/api/admin/seat-status")
                .param("buildingId", "1")
                .param("floor", "1")
                .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }
} 
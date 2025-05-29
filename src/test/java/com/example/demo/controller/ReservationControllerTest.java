package com.example.demo.controller;

import com.example.demo.dto.ReservationRequest;
import com.example.demo.dto.ReservationResponse;
import com.example.demo.service.ReservationService;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ReservationControllerTest {

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationController reservationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(reservationController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createReservation_ShouldReturnReservationResponse() throws Exception {
        ReservationRequest request = new ReservationRequest();
        request.setEmployeeId(1L);
        request.setSeatId(1L);
        request.setStartTime(LocalDateTime.now());
        request.setEndTime(LocalDateTime.now().plusHours(1));

        ReservationResponse response = new ReservationResponse();
        response.setId(1L);
        response.setEmployeeId(1L);
        response.setSeatId(1L);
        response.setStartTime(request.getStartTime());
        response.setEndTime(request.getEndTime());
        response.setStatus("PENDING");

        when(reservationService.createReservation(any(ReservationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeId").value(1))
                .andExpect(jsonPath("$.seatId").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void cancelReservation_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(post("/api/reservations/1/cancel")
                .param("employeeId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void getReservationsByEmployee_ShouldReturnListOfReservations() throws Exception {
        List<ReservationResponse> responses = Arrays.asList(
            new ReservationResponse(),
            new ReservationResponse()
        );

        when(reservationService.getReservationsByEmployee(1L)).thenReturn(responses);

        mockMvc.perform(get("/api/reservations/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAvailableSeats_ShouldReturnListOfSeatIds() throws Exception {
        List<Long> availableSeats = Arrays.asList(1L, 2L);

        when(reservationService.getAvailableSeats(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), any(), any())).thenReturn(availableSeats);

        mockMvc.perform(get("/api/reservations/available-seats")
                .param("start", LocalDateTime.now().toString())
                .param("end", LocalDateTime.now().plusHours(1).toString())
                .param("seatIds", "1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void extendReservation_ShouldReturnReservationResponse() throws Exception {
        ReservationResponse response = new ReservationResponse();
        response.setId(1L);
        response.setEmployeeId(1L);
        response.setSeatId(1L);
        response.setStartTime(LocalDateTime.now());
        response.setEndTime(LocalDateTime.now().plusHours(1));
        response.setStatus("IN_USE");

        when(reservationService.extendReservation(any(Long.class), any(LocalDateTime.class), any(Long.class))).thenReturn(response);

        mockMvc.perform(post("/api/reservations/1/extend")
                .param("newEndTime", LocalDateTime.now().plusHours(1).toString())
                .param("employeeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeId").value(1))
                .andExpect(jsonPath("$.seatId").value(1))
                .andExpect(jsonPath("$.status").value("IN_USE"));
    }

    @Test
    void getReservationHistory_ShouldReturnListOfReservations() throws Exception {
        List<ReservationResponse> responses = Arrays.asList(
            new ReservationResponse(),
            new ReservationResponse()
        );

        when(reservationService.getReservationHistory(any(Long.class), any(LocalDateTime.class), any(LocalDateTime.class), any(), any())).thenReturn(responses);

        mockMvc.perform(get("/api/reservations/history")
                .param("employeeId", "1")
                .param("start", LocalDateTime.now().toString())
                .param("end", LocalDateTime.now().plusHours(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void checkIn_ShouldReturnReservationResponse() throws Exception {
        ReservationResponse response = new ReservationResponse();
        response.setId(1L);
        response.setEmployeeId(1L);
        response.setSeatId(1L);
        response.setStartTime(LocalDateTime.now());
        response.setEndTime(LocalDateTime.now().plusHours(1));
        response.setStatus("IN_USE");

        when(reservationService.checkIn(any(Long.class), any(Long.class))).thenReturn(response);

        mockMvc.perform(post("/api/reservations/1/checkin")
                .param("employeeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeId").value(1))
                .andExpect(jsonPath("$.seatId").value(1))
                .andExpect(jsonPath("$.status").value("IN_USE"));
    }

    @Test
    void returnSeat_ShouldReturnReservationResponse() throws Exception {
        ReservationResponse response = new ReservationResponse();
        response.setId(1L);
        response.setEmployeeId(1L);
        response.setSeatId(1L);
        response.setStartTime(LocalDateTime.now());
        response.setEndTime(LocalDateTime.now().plusHours(1));
        response.setStatus("COMPLETED");

        when(reservationService.returnSeat(any(Long.class), any(Long.class))).thenReturn(response);

        mockMvc.perform(post("/api/reservations/1/return")
                .param("employeeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.employeeId").value(1))
                .andExpect(jsonPath("$.seatId").value(1))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
} 
package com.example.demo.dto;

import java.time.LocalDateTime;

public class ReservationResponse {
    public Long id;
    public Long employeeId;
    public Long seatId;
    public LocalDateTime startTime;
    public LocalDateTime endTime;
    public String status;
} 
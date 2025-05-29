package com.example.demo.dto;

import java.time.LocalDateTime;

public class ReservationRequest {
    public Long employeeId;
    public Long seatId;
    public LocalDateTime startTime;
    public LocalDateTime endTime;
} 
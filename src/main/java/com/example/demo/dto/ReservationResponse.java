package com.example.demo.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationResponse {
    public Long id;
    public Long employeeId;
    public Long seatId;
    public LocalDateTime startTime;
    public LocalDateTime endTime;
    public String status;
}

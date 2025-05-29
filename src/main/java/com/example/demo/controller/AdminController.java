package com.example.demo.controller;

import com.example.demo.entity.Seat;
import com.example.demo.service.SeatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final SeatService seatService;

    public AdminController(SeatService seatService) {
        this.seatService = seatService;
    }

    // A0101: 강제 좌석 반납
    @PostMapping("/force-return/{seatId}")
    public String forceReturnSeat(@PathVariable Long seatId) {
        seatService.forceReturnSeat(seatId);
        return "강제 반납 완료";
    }

    // A0102: 좌석 상세 현황 조회
    @GetMapping("/seat-status")
    public List<Seat> getDetailedSeatStatus(@RequestParam Long buildingId, @RequestParam int floor) {
        List<Seat> seat = seatService.getSeatsByBuildingAndFloor(buildingId, floor);
        return seat;
    }
} 
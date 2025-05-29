package com.example.demo.controller;

import com.example.demo.entity.Seat;
import com.example.demo.entity.Building;
import com.example.demo.service.SeatService;
import com.example.demo.service.BuildingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
public class SeatController {
    private final SeatService seatService;
    private final BuildingService buildingService;

    public SeatController(SeatService seatService, BuildingService buildingService) {
        this.seatService = seatService;
        this.buildingService = buildingService;
    }

    // U0201: 좌석 현황 조회
    @GetMapping("/building/{buildingId}/floor/{floor}/seats")
    public ResponseEntity<?> getSeatsByBuildingAndFloor(@PathVariable Long buildingId, @PathVariable int floor) {
        try {
            List<Seat> seats = seatService.getSeatsByBuildingAndFloor(buildingId, floor);
            return ResponseEntity.ok(seats);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // U0203: 전체 빌딩 조회
    @GetMapping("/buildings")
    public List<Building> getAllBuildings() {
        return buildingService.getAllBuildings();
    }
} 
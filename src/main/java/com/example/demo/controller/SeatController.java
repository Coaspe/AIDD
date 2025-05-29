package com.example.demo.controller;

import com.example.demo.entity.Seat;
import com.example.demo.entity.Building;
import com.example.demo.service.SeatService;
import com.example.demo.service.BuildingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public List<Seat> getSeatsByBuildingAndFloor(@PathVariable Long buildingId, @PathVariable int floor) {
        return seatService.getSeatsByBuildingAndFloor(buildingId, floor);
    }

    // U0203: 전체 빌딩 조회
    @GetMapping("/buildings")
    public List<Building> getAllBuildings() {
        return buildingService.getAllBuildings();
    }

} 
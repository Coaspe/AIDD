package com.example.demo.controller;

import com.example.demo.dto.ReservationRequest;
import com.example.demo.dto.ReservationResponse;
import com.example.demo.service.ReservationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/floors")
public class FloorController {
    private final FloorService floorService;

    public FloorController(FloorService floorService) {
        this.floorService = floorService;
    }
    
    // U0204: 층 조회
    @GetMapping("/building/{buildingId}")
    public List<Floor> getFloorsByBuilding(@PathVariable Long buildingId) {
        return floorService.getFloorsByBuilding(buildingId);
    }
}


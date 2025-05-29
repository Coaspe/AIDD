package com.example.demo.service;

import com.example.demo.entity.Floor;
import com.example.demo.repository.FloorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FloorService {
    private final FloorRepository floorRepository;

    public FloorService(FloorRepository floorRepository) {
        this.floorRepository = floorRepository;
    }

    // U0204: 층 조회
    public List<Floor> getFloorsByBuilding(Long buildingId) {
        return floorRepository.findByBuildingId(buildingId);
    }
}
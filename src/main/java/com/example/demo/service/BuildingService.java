package com.example.demo.service;

import com.example.demo.entity.Building;
import com.example.demo.repository.BuildingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuildingService {
    private final BuildingRepository buildingRepository;

    public BuildingService(BuildingRepository buildingRepository) {
        this.buildingRepository = buildingRepository;
    }

    // U0203: 전체 빌딩 조회
    public List<Building> getAllBuildings() {
        return buildingRepository.findAll();
    }
} 
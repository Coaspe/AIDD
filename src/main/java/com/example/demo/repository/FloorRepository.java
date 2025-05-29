package com.example.demo.repository;

import com.example.demo.entity.Floor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FloorRepository extends JpaRepository<Floor, Long> {
    List<Floor> findByBuildingId(Long buildingId);
    Optional<Floor> findByBuildingIdAndFloor(Long buildingId, int floor);
}
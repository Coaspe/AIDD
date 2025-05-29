package com.example.demo.service;

import com.example.demo.entity.Floor;
import com.example.demo.entity.Seat;
import com.example.demo.repository.SeatRepository;
import com.example.demo.repository.FloorRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.entity.Reservation;
import com.example.demo.entity.ReservationStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SeatService {
    private final SeatRepository seatRepository;
    private final ReservationRepository reservationRepository;
    private final FloorRepository floorRepository;

    public SeatService(SeatRepository seatRepository, ReservationRepository reservationRepository, FloorRepository floorRepository) {
        this.seatRepository = seatRepository;
        this.reservationRepository = reservationRepository;
        this.floorRepository = floorRepository;
    }

    // A0101: 강제 좌석 반납
    @Transactional
    public void forceReturnSeat(Long seatId) {
        // IN_USE 상태의 예약을 찾아 FORCED_CANCEL 처리
        List<Reservation> reservations = reservationRepository.findAll();
        for (Reservation r : reservations) {
            if (r.getSeatId().equals(seatId) && r.getStatus() == ReservationStatus.IN_USE) {
                r.setStatus(ReservationStatus.FORCED_CANCEL);
                reservationRepository.save(r);
            }
        }
    }

    // U0201: 좌석 현황 조회
    public List<Seat> getSeatsByBuildingAndFloor(Long buildingId, int floor) {
        Floor floorEntity = floorRepository.findByBuildingIdAndFloor(buildingId, floor)
            .orElseThrow(() -> new IllegalArgumentException("해당 빌딩의 해당 층이 존재하지 않습니다."));
        return seatRepository.findByFloorId(floorEntity.getId());
    }
} 
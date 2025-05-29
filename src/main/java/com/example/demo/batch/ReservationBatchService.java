package com.example.demo.batch;

import com.example.demo.entity.Reservation;
import com.example.demo.entity.ReservationStatus;
import com.example.demo.repository.ReservationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ReservationBatchService {
    private final ReservationRepository reservationRepository;

    public ReservationBatchService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    // 1분마다 실행 (예시)
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void updatePendingToReserved() {
        List<Reservation> pendings = reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.PENDING)
                .filter(r -> r.getStartTime().isBefore(LocalDateTime.now()))
                .toList();
        for (Reservation r : pendings) {
            r.setStatus(ReservationStatus.RESERVED);
            reservationRepository.save(r);
        }
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void updateReservedToNoShow() {
        List<Reservation> reserved = reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.RESERVED)
                .filter(r -> r.getStartTime().plusMinutes(10).isBefore(LocalDateTime.now()) && r.getCheckInAt() == null)
                .toList();
        for (Reservation r : reserved) {
            r.setStatus(ReservationStatus.NO_SHOW);
            reservationRepository.save(r);
        }
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void updateInUseToCompleted() {
        List<Reservation> inUse = reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.IN_USE)
                .filter(r -> r.getEndTime().isBefore(LocalDateTime.now()))
                .toList();
        for (Reservation r : inUse) {
            r.setStatus(ReservationStatus.COMPLETED);
            reservationRepository.save(r);
        }
    }
} 
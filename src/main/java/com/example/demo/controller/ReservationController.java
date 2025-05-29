package com.example.demo.controller;

import com.example.demo.dto.ReservationRequest;
import com.example.demo.dto.ReservationResponse;
import com.example.demo.service.ReservationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // U0102 예약 등록
    @PostMapping
    public ReservationResponse createReservation(@RequestBody ReservationRequest req) {
        return reservationService.createReservation(req);
    }

    // U0103: 예약 취소
    @PostMapping("/{id}/cancel")
    public void cancelReservation(@PathVariable Long id, @RequestParam Long employeeId) {
        reservationService.cancelReservation(id, employeeId);
    }
    
    // U0106 내 예약 현황 조회
    @GetMapping("/employee/{employeeId}")
    public List<ReservationResponse> getReservationsByEmployee(@PathVariable Long employeeId) {
        return reservationService.getReservationsByEmployee(employeeId);
    }

    // U0101: 예약 가능 좌석 기간 조회
    @GetMapping("/available-seats")
    public List<Long> getAvailableSeats(@RequestParam("start") String start,
                                        @RequestParam("end") String end,
                                        @RequestParam("seatIds") List<Long> seatIds,
                                        @RequestParam(value = "skip", required = false) Integer skip,
                                        @RequestParam(value = "limit", required = false) Integer limit) {
        return reservationService.getAvailableSeats(
            java.time.LocalDateTime.parse(start),
            java.time.LocalDateTime.parse(end),
            seatIds,
            skip,
            limit
        );
    }

    // U0104: 예약 시간 연장
    @PostMapping("/{id}/extend")
    public ReservationResponse extendReservation(@PathVariable Long id, 
                                               @RequestParam("newEndTime") String newEndTime,
                                               @RequestParam("employeeId") Long employeeId) {
        return reservationService.extendReservation(id, java.time.LocalDateTime.parse(newEndTime), employeeId);
    }

    // U0105: 예약 이력 조회
    @GetMapping("/history")
    public List<ReservationResponse> getReservationHistory(@RequestParam Long employeeId,
                                                          @RequestParam String start,
                                                          @RequestParam String end,
                                                          @RequestParam(value = "skip", required = false) Integer skip,
                                                          @RequestParam(value = "limit", required = false) Integer limit) {
        return reservationService.getReservationHistory(
            employeeId, 
            java.time.LocalDateTime.parse(start), 
            java.time.LocalDateTime.parse(end),
            skip,
            limit
        );
    }

    // U0202: 체크인
    @PostMapping("/{id}/checkin")
    public ReservationResponse checkIn(@PathVariable Long id, @RequestParam Long employeeId) {
        return reservationService.checkIn(id, employeeId);
    }

    // U0205: 좌석 반납
    @PostMapping("/{id}/return")
    public ReservationResponse returnSeat(@PathVariable Long id, @RequestParam Long employeeId) {
        return reservationService.returnSeat(id, employeeId);
    }
}

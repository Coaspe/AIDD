package com.example.demo.service;

import com.example.demo.entity.Reservation;
import com.example.demo.entity.ReservationStatus;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.dto.ReservationRequest;
import com.example.demo.dto.ReservationResponse;
import com.example.demo.entity.Seat;
import com.example.demo.entity.SeatStatus;
import com.example.demo.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;

    public ReservationService(ReservationRepository reservationRepository, SeatRepository seatRepository) {
        this.reservationRepository = reservationRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional
    public ReservationResponse createReservation(ReservationRequest req) {
        // 입력 데이터 검증
        if (req.employeeId == null || req.seatId == null || req.startTime == null || req.endTime == null) {
            throw new IllegalArgumentException("필수 입력값이 누락되었습니다.");
        }
        if (!req.startTime.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("예약 시작 시간은 현재 시간보다 미래여야 합니다.");
        }
        if (!req.endTime.isAfter(req.startTime)) {
            throw new IllegalArgumentException("종료 시간은 시작 시간보다 이후여야 합니다.");
        }
        if (req.endTime.minusHours(8).isAfter(req.startTime)) {
            throw new IllegalArgumentException("예약은 최대 8시간까지 가능합니다.");
        }

        // 좌석 상태 확인 (BROKEN 불가)
        Seat seat = seatRepository.findById(req.seatId)
                .orElseThrow(() -> new IllegalArgumentException("좌석 정보가 없습니다."));
        if (seat.getStatus() == SeatStatus.BROKEN) {
            throw new IllegalArgumentException("고장난 좌석은 예약할 수 없습니다.");
        }

        // 같은 좌석에 대해 다른 임직원의 RESERVED, IN_USE 예약과 겹치면 안 됨
        List<Reservation> overlapping = reservationRepository.findAll().stream()
                .filter(r -> r.getSeatId().equals(req.seatId))
                .filter(r -> r.getStatus() == ReservationStatus.RESERVED || r.getStatus() == ReservationStatus.IN_USE)
                .filter(r -> !(r.getEndTime().isBefore(req.startTime) || r.getStartTime().isAfter(req.endTime)))
                .collect(Collectors.toList());
        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException("해당 좌석은 이미 예약되어 있습니다.");
        }

        // 1인 1일 최대 8시간 제한 (해당 날짜의 예약 총합)
        LocalDateTime dayStart = req.startTime.toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        long totalSeconds = reservationRepository.findByEmployeeId(req.employeeId).stream()
                .filter(r -> !r.getStartTime().isAfter(dayEnd) && !r.getEndTime().isBefore(dayStart))
                .mapToLong(r -> Math.min(r.getEndTime().toEpochSecond(java.time.ZoneOffset.UTC), dayEnd.toEpochSecond(java.time.ZoneOffset.UTC))
                        - Math.max(r.getStartTime().toEpochSecond(java.time.ZoneOffset.UTC), dayStart.toEpochSecond(java.time.ZoneOffset.UTC)))
                .sum();
        long newSeconds = req.endTime.toEpochSecond(java.time.ZoneOffset.UTC) - req.startTime.toEpochSecond(java.time.ZoneOffset.UTC);
        if ((totalSeconds + newSeconds) > 8 * 3600) {
            throw new IllegalArgumentException("1일 최대 예약 가능 시간(8시간)을 초과합니다.");
        }

        // 예약 저장
        Reservation reservation = new Reservation();
        reservation.setEmployeeId(req.employeeId);
        reservation.setSeatId(req.seatId);
        reservation.setStartTime(req.startTime);
        reservation.setEndTime(req.endTime);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setCreatedAt(LocalDateTime.now());
        Reservation saved = reservationRepository.save(reservation);
        return toResponse(saved);
    }

    @Transactional
    public void cancelReservation(Long id, Long employeeId) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        // RESERVED 상태만 취소 가능
        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new IllegalStateException("예약 취소는 RESERVED 상태에서만 가능합니다.");
        }
        // 예약자와 요청자 id 일치 검증
        if (!reservation.getEmployeeId().equals(employeeId)) {
            throw new IllegalArgumentException("예약 취소 권한이 없습니다.");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    public List<ReservationResponse> getReservationsByEmployee(Long employeeId) {
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
        return reservationRepository.findByEmployeeId(employeeId).stream()
                .filter(r -> !r.getStartTime().isBefore(today))
                .filter(r -> r.getStatus() == ReservationStatus.PENDING
                          || r.getStatus() == ReservationStatus.RESERVED
                          || r.getStatus() == ReservationStatus.IN_USE)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // U0101: 예약 가능 좌석 기간 조회
    public List<Long> getAvailableSeats(
            LocalDateTime start, LocalDateTime end, 
            List<Long> allSeatIds, 
            Integer skip, Integer limit, 
            SeatRepository seatRepository) {

        // 1. 입력 데이터 검증
        if (start == null || end == null || start.isBefore(LocalDateTime.now()) || end.isBefore(start)) {
            throw new IllegalArgumentException("잘못된 시간 입력입니다.");
        }
        if (allSeatIds == null || allSeatIds.isEmpty()) {
            throw new IllegalArgumentException("좌석 목록이 필요합니다.");
        }

        // 기본값 처리
        int skipVal = (skip == null) ? 0 : skip;
        int limitVal = (limit == null) ? 5 : limit;

        // 2. BROKEN 좌석 제외
        List<Long> brokenSeatIds = seatRepository.findAll().stream()
                .filter(seat -> seat.getStatus() == SeatStatus.BROKEN)
                .map(Seat::getId)
                .collect(Collectors.toList());

        // 3. 예약 충돌 좌석 제외 (RESERVED, IN_USE)
        List<Reservation> overlapping = reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() == ReservationStatus.RESERVED || r.getStatus() == ReservationStatus.IN_USE)
                .filter(r -> !(r.getEndTime().isBefore(start) || r.getStartTime().isAfter(end)))
                .collect(Collectors.toList());
        List<Long> reservedSeatIds = overlapping.stream().map(Reservation::getSeatId).collect(Collectors.toList());

        // 4. 사용 가능한 좌석 필터링
        List<Long> available = allSeatIds.stream()
                .filter(id -> !reservedSeatIds.contains(id))
                .filter(id -> !brokenSeatIds.contains(id))
                .skip(skipVal)
                .limit(limitVal)
                .collect(Collectors.toList());

        return available;
    }

    // U0104: 예약 시간 연장
    @Transactional
    public ReservationResponse extendReservation(Long id, LocalDateTime newEndTime, Long employeeId) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        // IN_USE 상태만 연장 가능
        if (reservation.getStatus() != ReservationStatus.IN_USE) {
            throw new IllegalStateException("예약 시간 연장은 IN_USE 상태에서만 가능합니다.");
        }
        // 예약자와 요청자 id 일치 검증
        if (!reservation.getEmployeeId().equals(employeeId)) {
            throw new IllegalArgumentException("예약 연장 권한이 없습니다.");
        }
        // 좌석 상태 확인 (UNAVAILABLE 불가)
        Seat seat = seatRepository.findById(reservation.getSeatId())
                .orElseThrow(() -> new IllegalArgumentException("좌석 정보가 없습니다."));
        if (seat.getStatus() == SeatStatus.UNAVAILABLE) {
            throw new IllegalArgumentException("사용 불가 좌석은 연장할 수 없습니다.");
        }
        // 기존 예약 종료시간에서 1시간만 연장 가능
        if (!newEndTime.equals(reservation.getEndTime().plusHours(1))) {
            throw new IllegalArgumentException("예약 연장은 1시간 단위로만 가능합니다.");
        }
        // 연장 후 종료시간이 다음날 0시를 넘을 수 없음
        LocalDateTime dayEnd = reservation.getStartTime().toLocalDate().atStartOfDay().plusDays(1);
        if (!newEndTime.isBefore(dayEnd)) {
            throw new IllegalArgumentException("연장 후 종료시간은 다음날 0시를 넘을 수 없습니다.");
        }
        // 같은 좌석의 다른 임직원 RESERVED, IN_USE 예약과 겹치면 안 됨
        List<Reservation> overlapping = reservationRepository.findAll().stream()
                .filter(r -> r.getSeatId().equals(reservation.getSeatId()))
                .filter(r -> r.getStatus() == ReservationStatus.RESERVED || r.getStatus() == ReservationStatus.IN_USE)
                .filter(r -> !r.getId().equals(id))
                .filter(r -> !(r.getEndTime().isBefore(reservation.getEndTime()) || r.getStartTime().isAfter(newEndTime)))
                .collect(Collectors.toList());
        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException("해당 좌석은 이미 예약되어 있습니다.");
        }
        // 1인 1일 최대 8시간 제한 (연장 포함)
        LocalDateTime dayStart = reservation.getStartTime().toLocalDate().atStartOfDay();
        long totalSeconds = reservationRepository.findByEmployeeId(employeeId).stream()
                .filter(r -> !r.getStartTime().isAfter(dayEnd) && !r.getEndTime().isBefore(dayStart))
                .mapToLong(r -> Math.min(r.getEndTime().toEpochSecond(java.time.ZoneOffset.UTC), dayEnd.toEpochSecond(java.time.ZoneOffset.UTC))
                        - Math.max(r.getStartTime().toEpochSecond(java.time.ZoneOffset.UTC), dayStart.toEpochSecond(java.time.ZoneOffset.UTC)))
                .sum();
        long newSeconds = newEndTime.toEpochSecond(java.time.ZoneOffset.UTC) - reservation.getStartTime().toEpochSecond(java.time.ZoneOffset.UTC);
        if (newSeconds > 8 * 3600 || totalSeconds > 8 * 3600) {
            throw new IllegalArgumentException("1일 최대 예약 가능 시간(8시간)을 초과합니다.");
        }
        // 연장 예약 생성
        Reservation extended = new Reservation();
        extended.setEmployeeId(reservation.getEmployeeId());
        extended.setSeatId(reservation.getSeatId());
        extended.setStartTime(reservation.getStartTime());
        extended.setEndTime(newEndTime);
        extended.setStatus(ReservationStatus.IN_USE);
        extended.setCreatedAt(LocalDateTime.now());
        extended.setExtendedFromReservationId(reservation.getId());
        Reservation saved = reservationRepository.save(extended);
        return toResponse(saved);
    }

    // U0105: 예약 이력 조회
    public List<ReservationResponse> getReservationHistory(Long employeeId, LocalDateTime start, LocalDateTime end, Integer skip, Integer limit) {
        // 입력 데이터 검증
        if (employeeId == null || start == null || end == null) {
            throw new IllegalArgumentException("필수 입력값이 누락되었습니다.");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("종료일자가 시작일자보다 빠를 수 없습니다.");
        }
        int skipVal = (skip == null) ? 0 : skip;
        int limitVal = (limit == null) ? 5 : limit;
        return reservationRepository.findByEmployeeId(employeeId).stream()
            .filter(r -> !r.getStartTime().isAfter(end) && !r.getEndTime().isBefore(start))
            .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
            .skip(skipVal)
            .limit(limitVal)
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    // U0202: 체크인
    @Transactional
    public ReservationResponse checkIn(Long id, Long employeeId) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        // 체크인 수행하는 좌석은 AVAILABLE이어야 함
        Seat seat = seatRepository.findById(reservation.getSeatId())
                .orElseThrow(() -> new IllegalArgumentException("좌석 정보가 없습니다."));
        if (seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new IllegalStateException("체크인 가능한 좌석이 아닙니다.");
        }
        // 체크인 수행하는 예약은 RESERVED 상태여야 함
        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new IllegalStateException("체크인 가능한 예약이 아닙니다.");
        }
        // 체크인 요청자와 예약자 id 일치 검증
        if (!reservation.getEmployeeId().equals(employeeId)) {
            throw new IllegalArgumentException("체크인 권한이 없습니다.");
        }
        // 상태 변경
        reservation.setStatus(ReservationStatus.IN_USE);
        reservation.setCheckInAt(java.time.LocalDateTime.now());
        seat.setStatus(SeatStatus.UNAVAILABLE);
        seatRepository.save(seat);
        Reservation saved = reservationRepository.save(reservation);
        return toResponse(saved);
    }

    // U0205: 좌석 반납
    @Transactional
    public ReservationResponse returnSeat(Long id, Long employeeId) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        // IN_USE 상태만 반납 가능
        if (reservation.getStatus() != ReservationStatus.IN_USE) {
            throw new IllegalStateException("좌석 반납은 IN_USE 상태에서만 가능합니다.");
        }
        // 좌석 UNAVAILABLE 상태만 반납 가능
        Seat seat = seatRepository.findById(reservation.getSeatId())
                .orElseThrow(() -> new IllegalArgumentException("좌석 정보가 없습니다."));
        if (seat.getStatus() != SeatStatus.UNAVAILABLE) {
            throw new IllegalStateException("좌석 반납은 UNAVAILABLE 상태에서만 가능합니다.");
        }
        // 반납 요청자와 예약자 id 일치 검증
        if (!reservation.getEmployeeId().equals(employeeId)) {
            throw new IllegalArgumentException("좌석 반납 권한이 없습니다.");
        }
        // 해당 좌석의 모든 IN_USE 예약을 COMPLETED로 변경
        List<Reservation> inUseReservations = reservationRepository.findAll().stream()
                .filter(r -> r.getSeatId().equals(seat.getId()))
                .filter(r -> r.getStatus() == ReservationStatus.IN_USE)
                .collect(Collectors.toList());
        for (Reservation r : inUseReservations) {
            r.setStatus(ReservationStatus.COMPLETED);
            reservationRepository.save(r);
        }
        // 좌석 상태 AVAILABLE로 변경
        seat.setStatus(SeatStatus.AVAILABLE);
        seatRepository.save(seat);
        // 반환된 예약 정보 반환 (반납 요청한 예약)
        reservation.setStatus(ReservationStatus.COMPLETED);
        Reservation saved = reservationRepository.save(reservation);
        return toResponse(saved);
    }

    private ReservationResponse toResponse(Reservation reservation) {
        ReservationResponse res = new ReservationResponse();
        res.id = reservation.getId();
        res.employeeId = reservation.getEmployeeId();
        res.seatId = reservation.getSeatId();
        res.startTime = reservation.getStartTime();
        res.endTime = reservation.getEndTime();
        res.status = reservation.getStatus().name();
        return res;
    }
}

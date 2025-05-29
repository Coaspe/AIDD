package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "floor_id")
    private Floor floor;

    private String name;
    @Enumerated(EnumType.STRING)
    private SeatStatus status; // AVAILABLE, UNAVAILABLE, BROKEN 등
}
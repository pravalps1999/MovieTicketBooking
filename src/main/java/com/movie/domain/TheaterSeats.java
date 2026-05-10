package com.movie.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.movie.enums.SeatType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "theater_seats",uniqueConstraints = {
            @UniqueConstraint(columnNames = {"seat_number", "theater_id"})
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TheaterSeats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seat_number",nullable = false)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type",nullable = false)
    private SeatType seatType;

    @ManyToOne
    @JoinColumn(name = "theater_id", nullable = false)
    private Theater theater;
}

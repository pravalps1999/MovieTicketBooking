package com.movie.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.movie.enums.SeatStatus;
import com.movie.enums.SeatType;
import com.movie.resource.ShowSeatLayoutResource;
import com.movie.resource.ShowSeatsResource;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Entity
@Table(name = "show_seats")
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ShowSeats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seat_number",nullable = false)
    private String seatNumber;

    @Column(name = "rate",nullable = false)
    private int rate;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type",nullable = false)
    private SeatType seatType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

    private LocalDateTime lockedAt;

    @Column(name = "booked_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bookedAt;

    @ManyToOne
    @JsonIgnore
    private Show show;

    @ManyToOne
    @JsonIgnore
    private Ticket ticket;

    public static List<ShowSeatsResource> toResource(List<ShowSeats> seats){
        if(!CollectionUtils.isEmpty(seats)){
            return seats.stream().map(ShowSeats::toResource).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
    public static ShowSeatsResource toResource(ShowSeats seatEntity){
        return ShowSeatsResource.builder()
                .seatId(seatEntity.getId())
                .rate(seatEntity.getRate())
                .seatNumber(seatEntity.getSeatNumber())
                .seatType(seatEntity.getSeatType())
                .status(seatEntity.getStatus())
                .bookedAt(seatEntity.getBookedAt())
                .build();
    }
    public static ShowSeatLayoutResource toLayoutResponse(ShowSeats seat) {
        return ShowSeatLayoutResource.builder()
                .seatNumber(seat.getSeatNumber())
                .seatType(seat.getSeatType())
                .status(seat.getStatus())
                .build();
    }
}

package com.movie.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.movie.resource.TheaterResource;
import com.movie.resource.TheaterSeatsResource;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "theaters")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Theater {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name",nullable = false)
    private String name;

    @Column(name = "city",nullable = false)
    private String city;

    @Column(name = "address",nullable = false)
    private String address;

    @OneToMany(mappedBy = "theater",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    @ToString.Exclude
    private List<Show> shows=new ArrayList<>();

    @OneToMany(mappedBy = "theater",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    @ToString.Exclude
    private List<TheaterSeats> seats=new ArrayList<>();

    public static Theater toEntity(TheaterResource theaterResource){
        return Theater.builder()
                .name(theaterResource.getName())
                .city(theaterResource.getCity())
                .address(theaterResource.getAddress())
                .build();
    }

    public static TheaterResource toResource(Theater theater){
        return TheaterResource.builder()
                .theaterId(theater.getId())
                .name(theater.getName())
                .city(theater.getCity())
                .address(theater.getAddress())
                .seats(theater.getSeats()
                        .stream()
                        .map(seat -> TheaterSeatsResource.builder()
                                .seatNumber(seat.getSeatNumber())
                                .seatType(seat.getSeatType().name())
                                .build())
                        .toList()
                )
                .build();
    }
}


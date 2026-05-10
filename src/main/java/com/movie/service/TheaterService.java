package com.movie.service;

import com.movie.Repository.TheaterRepository;
import com.movie.domain.Theater;
import com.movie.domain.TheaterSeats;
import com.movie.enums.SeatType;
import com.movie.exception.ResourceNotFoundException;
import com.movie.resource.TheaterResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TheaterService {
    @Autowired
    private TheaterRepository theaterRepository;

    @Transactional
    public TheaterResource addTheater(TheaterResource theaterResource) {
        Theater theater = Theater.toEntity(theaterResource);
        //generate seats
        generateSeats(theater);
        Theater addedTheater = theaterRepository.save(theater);
        return Theater.toResource(addedTheater);
    }
    @Transactional(readOnly = true)
    public TheaterResource getTheater(Long id) {
        Theater theater=theaterRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Theater not found with id: " + id));
        return Theater.toResource(theater);
    }
    private void generateSeats(Theater theater) {
        if (theater.getSeats() != null && !theater.getSeats().isEmpty()) {
            return;
        }
        List<TheaterSeats> seats = new ArrayList<>();
        // SILVER (A-C)
        seats.addAll(createSeats(theater, "A", 10, SeatType.SILVER));
        seats.addAll(createSeats(theater, "B", 10, SeatType.SILVER));
        seats.addAll(createSeats(theater, "C", 10, SeatType.SILVER));
        // GOLD (D-F)
        seats.addAll(createSeats(theater, "D", 10, SeatType.GOLD));
        seats.addAll(createSeats(theater, "E", 10, SeatType.GOLD));
        seats.addAll(createSeats(theater, "F", 10, SeatType.GOLD));
        // PLATINUM (G-H)
        seats.addAll(createSeats(theater, "G", 10, SeatType.PLATINUM));
        seats.addAll(createSeats(theater, "H", 10, SeatType.PLATINUM));
        // RECLINER (I)
        seats.addAll(createSeats(theater, "I", 5, SeatType.RECLINER));
        theater.setSeats(seats);
    }
    private List<TheaterSeats> createSeats(Theater theater,String row,int count,SeatType type) {
        List<TheaterSeats> seatList = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            seatList.add(TheaterSeats.builder()
                    .seatNumber(row + i)
                    .seatType(type)
                    .theater(theater)   // VERY IMPORTANT
                    .build());
        }
        return seatList;
    }
}

package com.movie.resource;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.movie.enums.SeatStatus;
import com.movie.enums.SeatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShowSeatsResource {
    private Long seatId;
    private String seatNumber;
    private int rate;
    private SeatType seatType;
    private SeatStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bookedAt;
}

package com.movie.resource;

import com.movie.enums.SeatStatus;
import com.movie.enums.SeatType;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShowSeatLayoutResource {

        private String seatNumber;

        private SeatType seatType;

        private SeatStatus status;
}

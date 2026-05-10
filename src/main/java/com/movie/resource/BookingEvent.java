package com.movie.resource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingEvent {
    private Long userId;
    private Long showId;
    private List<String> seatNumbers;
    private int retryCount;
}

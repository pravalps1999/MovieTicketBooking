package com.movie.resource;

import com.movie.domain.Show;
import com.movie.enums.TicketStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TicketResource {
    private Long ticketId;

    private String movieName;
    private String theaterName;
    private String city;

    private String showTime;
    private String bookedAt;

    private List<String> seats;

    private Double totalAmount;

    private TicketStatus status;
}

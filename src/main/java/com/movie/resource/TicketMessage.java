package com.movie.resource;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.movie.domain.Ticket;
import com.movie.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketMessage {
    // User Info (IMPORTANT)
    private String username;
    private String email;
    private String mobile;

    // Ticket Info
    private Long ticketId;
    private String movieName;
    private String showTime;
    private String theaterName;
    private String allottedSeats;
    private String city;

    private Double totalAmount;
    private String bookedAt;
    private String status;

    public static TicketMessage from(Ticket ticket) {

        return TicketMessage.builder()
                .ticketId(ticket.getId())
                .username(ticket.getUser().getName())
                .email(ticket.getUser().getEmail())
                .mobile(ticket.getUser().getMobile())
                .movieName(ticket.getShow().getMovie().getTitle())
                .theaterName(ticket.getShow().getTheater().getName())
                .city(ticket.getShow().getTheater().getCity())
                .showTime(ticket.getShow().getShowTime().format(DateUtil.FORMATTER))
                .allottedSeats(ticket.getAllottedSeats())
                .totalAmount(ticket.getAmount())
                .status(ticket.getStatus().name())
                .bookedAt(LocalDateTime.now().format(DateUtil.FORMATTER))
                .build();
    }
}

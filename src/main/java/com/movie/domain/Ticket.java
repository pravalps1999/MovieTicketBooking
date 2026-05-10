package com.movie.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.movie.enums.TicketStatus;
import com.movie.resource.TicketResource;
import com.movie.util.DateUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "tickets",
        indexes = {
                @Index(name = "idx_ticket_status", columnList = "status")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false)
    private double amount;

    @CreationTimestamp
    @Column(name = "booked_at", nullable = false, updatable = false)
    private LocalDateTime bookedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id")
    @JsonIgnore
    private Show show;

    @Column(name = "allotted_seats", nullable = false)
    private String allottedSeats;

    @OneToMany(mappedBy = "ticket")
    @JsonIgnore
    private List<ShowSeats> seats;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    public static Ticket toEntity(TicketResource resource){
        return Ticket.builder()
                .amount(resource.getTotalAmount())
                .build();
    }

    public static TicketResource toResource(Ticket ticket){
        return TicketResource.builder()
                .ticketId(ticket.getId())
                .movieName(ticket.getShow().getMovie().getTitle())
                .theaterName(ticket.getShow().getTheater().getName())
                .city(ticket.getShow().getTheater().getCity())
                .showTime(ticket.getShow().getShowTime().format(DateUtil.FORMATTER))
                .bookedAt(ticket.getBookedAt().format(DateUtil.FORMATTER))
                .seats(
                        ticket.getSeats() != null ?
                                ticket.getSeats()
                                        .stream()
                                        .map(ShowSeats::getSeatNumber)
                                        .toList()
                                : List.of()
                )
                .totalAmount(ticket.getAmount())
                .status(ticket.getStatus())
                .build();
    }
}
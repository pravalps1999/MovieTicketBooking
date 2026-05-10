package com.movie.resource;

import com.movie.enums.SeatType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResource {
    @NotNull(message = "User ID is required")
    @Min(value = 1, message = "Invalid user ID")
    private Long userId;

    @NotNull(message = "Show ID is required")
    @Min(value = 1, message = "Invalid show ID")
    private Long showId;

    @NotEmpty(message = "Seat numbers cannot be empty")
    private Set<@NotEmpty(message = "Seat cannot be blank") String> seatNumbers;

    private int retryCount=0; // IMPORTANT
}

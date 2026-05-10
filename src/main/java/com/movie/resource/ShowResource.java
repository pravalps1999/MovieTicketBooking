package com.movie.resource;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowResource {

    private Long showId;

    @NotNull(message = "Show time is mandatory")
    @JsonFormat(pattern = "dd-MM-yyyy hh:mm a")
    private LocalDateTime showTime;

    @NotNull(message = "Movie is mandatory for show")
    private Long movieId;

    @NotNull(message = "Theater is mandatory for show")
    private Long theaterId;

    // Keep ISO for internal/debugging (recommended)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
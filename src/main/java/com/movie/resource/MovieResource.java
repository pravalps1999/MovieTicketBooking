package com.movie.resource;

import com.movie.domain.Review;
import com.movie.enums.Genre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieResource {
    private Long movieId;
    private String title;
    private Genre genre;
    private Double rating;
}

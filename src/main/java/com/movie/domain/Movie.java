package com.movie.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.movie.enums.Genre;
import com.movie.resource.MovieResource;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    private Genre genre;
    @Builder.Default
    private Double rating=0.0;

    @OneToMany(mappedBy = "movie")
    private List<Review> reviews;

    @OneToMany(mappedBy = "movie",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Show> shows=new ArrayList<>();

    public static Movie toEntity(MovieResource movieResource){
        return Movie.builder()
                .title(movieResource.getTitle())
                .genre(movieResource.getGenre())
                .build();
    }

    public static MovieResource toResource(Movie movie){
        return MovieResource.builder()
                .movieId(movie.getId())
                .title(movie.getTitle())
                .genre(movie.getGenre())
                .rating(movie.getRating())
                .build();
    }

}

package com.movie.service;

import com.movie.Repository.MovieRepository;
import com.movie.domain.Movie;
import com.movie.enums.Genre;
import com.movie.exception.ResourceNotFoundException;
import com.movie.resource.MovieResource;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MovieService {
    @Autowired
    private MovieRepository movieRepository;

    public MovieResource addMovie(MovieResource movieResource) {
        Movie movie=movieRepository.save(Movie.toEntity(movieResource));
        return Movie.toResource(movie);
    }

    public MovieResource getMovieById(long id) {
        Movie movie=movieRepository.findById(id).orElseThrow(
                ()->new ResourceNotFoundException("Movie not found with id: " + id));
        return Movie.toResource(movie);
    }
    public MovieResource getMovieByTitle(String title) {
        Movie movie=movieRepository.findByTitle(title).orElseThrow(
                ()->new ResourceNotFoundException("Movie not found with title: " + title));
        return Movie.toResource(movie);
    }
    public List<MovieResource> searchByTitle(String title){
        return movieRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .sorted(Comparator.comparing(Movie::getRating).reversed())
                .map(Movie::toResource)
                .toList();
    }

    public List<MovieResource> searchByGenre(Genre genre) {
        return movieRepository.findByGenre(genre)
                .stream()
                .sorted(Comparator.comparing(Movie::getRating).reversed())
                .map(Movie::toResource)
                .toList();
    }
}

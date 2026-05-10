package com.movie.controller;

import com.movie.enums.Genre;
import com.movie.exception.InvalidGenreException;
import com.movie.resource.MovieResource;
import com.movie.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movie")
public class MovieController {
    @Autowired
    private MovieService movieService;

    @PostMapping("/add")
    public ResponseEntity<MovieResource> addMovie(@RequestBody MovieResource movieResource){
        return ResponseEntity.ok(movieService.addMovie(movieResource));
    }
    @GetMapping("/{id}")
    public ResponseEntity<MovieResource> getMovieById(@PathVariable(name = "id") long id){
        return ResponseEntity.ok(movieService.getMovieById(id));
    }
    @GetMapping("/search")
    public ResponseEntity<List<MovieResource>> searchMovies(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String genre){

        if (title != null) {
            return ResponseEntity.ok(movieService.searchByTitle(title));
        }
        if (genre != null) {
            Genre genre1;
            try {
                genre1 = Genre.valueOf(genre.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidGenreException("Invalid genre value: '" + genre +"', Allowed values: ACTION, COMEDY, DRAMA, SCI_FI, THRILLER, ROMANCE");
            }
            return ResponseEntity.ok(movieService.searchByGenre(genre1));
        }
        return ResponseEntity.badRequest().build();
    }
}

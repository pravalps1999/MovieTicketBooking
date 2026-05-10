package com.movie.service;

import com.movie.Repository.MovieRepository;
import com.movie.Repository.ReviewRepository;
import com.movie.domain.Movie;
import com.movie.domain.Review;
import com.movie.exception.ResourceNotFoundException;
import com.movie.resource.ReviewResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private MovieRepository movieRepository;

    public ReviewResource addReview(ReviewResource reviewResource) {
        Review review=Review.toEntity(reviewResource);
        Review addedReview=reviewRepository.save(review);
        Movie movie=movieRepository.findById(review.getMovie().getId()).orElseThrow(
                ()->new ResourceNotFoundException("Movie not found with id: " + review.getMovie().getId()));
        Double average =reviewRepository.getReviewAverage(movie.getId());
        movie.setRating(average);
        movieRepository.save(movie);
        return Review.toResource(addedReview);
    }
    public List<ReviewResource> getReviewsByMovie(Long movieId) {
        return reviewRepository.findByMovieId(movieId)
                .stream()
                .map(Review::toResource)
                .toList();
    }
}

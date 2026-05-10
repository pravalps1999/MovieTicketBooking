package com.movie.controller;

import com.movie.resource.ReviewResource;
import com.movie.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/review")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    @PostMapping("/add")
    public ResponseEntity<ReviewResource> addReview(@RequestBody ReviewResource reviewResource){
        return ResponseEntity.ok(reviewService.addReview(reviewResource));
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ReviewResource>> getReviewsByMovie(@PathVariable Long movieId){
        return ResponseEntity.ok(reviewService.getReviewsByMovie(movieId));
    }
}

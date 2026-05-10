package com.movie.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.movie.resource.ReviewResource;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Table(name = "reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    @Id
    @Column(name = "id",nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String movieReview;

    private Double rating;

    @ManyToOne
    @JoinColumn(name = "movie_id",nullable = false)
    @JsonIgnore
    private Movie movie;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedDate;

    public static Review toEntity(ReviewResource reviewResource){
        return Review.builder()
                .movieReview(reviewResource.getMovieReview())
                .rating(reviewResource.getRating())
                .movie(Movie.builder()
                        .id(reviewResource.getMovieId())
                        .build())
                .build();
    }
    public static ReviewResource toResource(Review review){
        return ReviewResource.builder()
                .reviewId(review.getId())
                .movieReview(review.getMovieReview())
                .rating(review.getRating())
                .movieId(review.getMovie().getId())
                .createdDate(review.getCreatedDate())
                .updatedDate(review.getUpdatedDate())
                .build();
    }
    public static List<ReviewResource> toResource(List<Review> reviewList){
        if(Objects.isNull(reviewList))return new ArrayList<>();
        else return  reviewList.stream().map(Review::toResource).collect(Collectors.toList());
    }
}

package com.movie.Repository;

import com.movie.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review,Long> {
    @Query(value = "select ROUND(AVG(rating), 2) from reviews where movie_id=?",nativeQuery = true)
    Double getReviewAverage(Long id);
    List<Review> findByMovieId(Long movieId);
}

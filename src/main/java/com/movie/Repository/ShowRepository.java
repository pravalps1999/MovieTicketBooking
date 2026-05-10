package com.movie.Repository;

import com.movie.domain.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show,Long> {
    List<Show> findByTheaterCityIgnoreCase(String city);
    List<Show> findByTheaterNameIgnoreCase(String name);
    @Query(value = """
                    SELECT 
                        s.id,s.created_at,
                        s.show_time,s.updated_at,
                        s.movie_id,s.theater_id 
                    FROM shows s 
                    JOIN theaters t 
                    ON s.theater_id=t.id 
                    WHERE t.name=? AND t.city=?
                                        """,nativeQuery = true)
    List<Show> findByTheaterNameAndCityName(String theaterName,String cityName);

}

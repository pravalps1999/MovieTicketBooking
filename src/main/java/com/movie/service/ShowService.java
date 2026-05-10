package com.movie.service;

import com.movie.Repository.MovieRepository;
import com.movie.Repository.ShowRepository;
import com.movie.Repository.TheaterRepository;
import com.movie.domain.*;
import com.movie.enums.SeatStatus;
import com.movie.enums.SeatType;
import com.movie.exception.ResourceNotFoundException;
import com.movie.resource.ShowResource;
import com.movie.resource.ShowSeatLayoutResource;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ShowService {
    @Autowired
    private ShowRepository showRepository;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private TheaterRepository theaterRepository;

    @Transactional
    public ShowResource addShow(ShowResource showResource) {

        Show show = Show.toEntity(showResource);

        Movie movie = movieRepository.findById(showResource.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        Theater theater = theaterRepository.findById(showResource.getTheaterId())
                .orElseThrow(() -> new RuntimeException("Theater not found"));

        show.setMovie(movie);
        show.setTheater(theater);
        List<ShowSeats> seats = generateSeatsFromTheater(theater, show);
        show.setSeats(seats);
        Show saved = showRepository.save(show);

        return Show.toResource(saved);
    }

    public List<ShowResource> getShowsByMovie(String movieName) {
        Movie movie=movieRepository.findByTitle(movieName).orElseThrow(
                ()->new ResourceNotFoundException("Movie not found with name: " + movieName));
        List<Show> shows=movie.getShows();
        return Show.toResource(shows);
    }

    public List<ShowResource> getShowsByCity(String cityName) {
        return showRepository.findByTheaterCityIgnoreCase(cityName)
                .stream()
                .sorted(Comparator.comparing(Show::getShowTime))
                .map(Show::toResource)
                .toList();
    }
    public List<ShowResource> getShowsByTheaterName(String theaterName) {
        return showRepository.findByTheaterNameIgnoreCase(theaterName)
                .stream()
                .sorted(Comparator.comparing(Show::getShowTime))
                .map(Show::toResource)
                .toList();
    }
    public List<ShowResource> getShowsByTheaterNameAndCityName(String theaterName,String cityName) {
        return showRepository.findByTheaterNameAndCityName(theaterName,cityName)
                .stream()
                .sorted(Comparator.comparing(Show::getShowTime))
                .map(Show::toResource)
                .toList();
    }

    public List<ShowSeatLayoutResource> getSeatLayout(Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id :"+showId));
        return show.getSeats()
                .stream()
                .map(ShowSeats::toLayoutResponse)
                .toList();
    }

    private List<ShowSeats> generateSeatsFromTheater(Theater theater, Show show) {

        List<ShowSeats> showSeats = new ArrayList<>();

        for (TheaterSeats ts : theater.getSeats()) {

            ShowSeats seat = new ShowSeats();
            seat.setSeatNumber(ts.getSeatNumber());
            seat.setSeatType(ts.getSeatType());
            seat.setRate(getRate(ts.getSeatType()));
            seat.setStatus(SeatStatus.AVAILABLE);

            // THIS IS MANDATORY
            seat.setShow(show);

            showSeats.add(seat);
        }

        return showSeats;
    }
    private int getRate(SeatType type) {
        return switch (type) {
            case SILVER -> 200;
            case GOLD -> 300;
            case PLATINUM -> 400;
            case RECLINER -> 600;
        };
    }
}

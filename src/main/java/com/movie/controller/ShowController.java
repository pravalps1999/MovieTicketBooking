package com.movie.controller;

import com.movie.resource.ShowResource;
import com.movie.resource.ShowSeatLayoutResource;
import com.movie.service.ShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/show")
public class ShowController {
    @Autowired
    private ShowService showService;

    @PostMapping("/add")
    public ResponseEntity<ShowResource> addShow(@RequestBody ShowResource showResource){
        return ResponseEntity.ok(showService.addShow(showResource));
    }

    @GetMapping("/movie")
    public ResponseEntity<List<ShowResource>> getShowsByMovie(@RequestParam String movieName){
        return ResponseEntity.ok(showService.getShowsByMovie(movieName));
    }

    @GetMapping("/city")
    public ResponseEntity<List<ShowResource>> getShowsByCity(@RequestParam(name = "cityName", required = true) String cityName){
        return ResponseEntity.ok(showService.getShowsByCity(cityName));
    }

    @GetMapping("/theater")
    public ResponseEntity<List<ShowResource>> getShowsByTheaterName(@RequestParam(name = "theaterName", required = true) String theaterName){
        return ResponseEntity.ok(showService.getShowsByTheaterName(theaterName));
    }

    @GetMapping("/theater/city")
    public ResponseEntity<List<ShowResource>> getShowsByTheaterNameAndCityName(@RequestParam(name = "theaterName", required = true) String theaterName,
                                                                               @RequestParam(name = "cityName", required = true) String cityName){
        return ResponseEntity.ok(showService.getShowsByTheaterNameAndCityName(theaterName, cityName));
    }

    @GetMapping("/{showId}/seats")
    public ResponseEntity<List<ShowSeatLayoutResource>> getSeatLayout(@PathVariable Long showId){
        return ResponseEntity.ok(showService.getSeatLayout(showId));
    }
}

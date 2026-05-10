package com.movie.controller;

import com.movie.resource.TheaterResource;
import com.movie.service.TheaterService;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/theater")
@Validated
public class TheaterController {
    @Autowired
    private TheaterService theaterService;

    @PostMapping("/add")
    public ResponseEntity<TheaterResource> addTheater(@RequestBody TheaterResource theaterResource){
        return ResponseEntity.ok(theaterService.addTheater(theaterResource));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TheaterResource> getTheater(@PathVariable(name = "id") @Min(value = 1, message = "Id must be greater than or equal to 1") Long id){
        return ResponseEntity.ok(theaterService.getTheater(id));
    }
}

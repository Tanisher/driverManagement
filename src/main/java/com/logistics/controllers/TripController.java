package com.logistics.controllers;

import com.logistics.DTO.DeadheadEndResponse;
import com.logistics.DTO.EndDeadheadRequest;
import com.logistics.DTO.EndLoadedTripRequest;
import com.logistics.DTO.StartDeadheadRequest;
import com.logistics.DTO.TripResponse;
import com.logistics.service.TripService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping("/deadhead/start")
    public ResponseEntity<TripResponse> startDeadhead(@Valid @RequestBody StartDeadheadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tripService.startDeadhead(request));
    }

    @PostMapping("/deadhead/{id}/end")
    public ResponseEntity<DeadheadEndResponse> endDeadhead(@PathVariable Long id,
                                                           @Valid @RequestBody EndDeadheadRequest request) {
        return ResponseEntity.ok(tripService.endDeadhead(id, request));
    }

    @PostMapping("/loaded/{id}/end")
    public ResponseEntity<TripResponse> endLoaded(@PathVariable Long id,
                                                  @Valid @RequestBody EndLoadedTripRequest request) {
        return ResponseEntity.ok(tripService.endLoaded(id, request));
    }
}

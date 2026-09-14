package com.logistics.controllers;

import com.logistics.DTO.ActualWeightRequest;
import com.logistics.DTO.LoadBillableAmountResponse;
import com.logistics.entity.Load;
import com.logistics.entity.LoadDTO;
import com.logistics.service.LoadService;
import com.logistics.util.DriverAssignmentRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loads")
public class LoadController {
    private final LoadService loadService;

    public LoadController(LoadService loadService) {
        this.loadService = loadService;
    }

    @PostMapping
    public ResponseEntity<LoadDTO> createLoad(@RequestBody Load load) {
        Load savedLoad = loadService.saveLoad(load);
        LoadDTO loadDTO = loadService.convertToDTO(savedLoad);
        return ResponseEntity.ok(loadDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LoadDTO> updateLoad(@PathVariable Long id, @RequestBody Load load) {
        Load updated = loadService.updateLoad(id, load);
        return ResponseEntity.ok(loadService.convertToDTO(updated));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<LoadDTO> patchLoad(@PathVariable Long id, @RequestBody LoadDTO patch) {
        Load updated = loadService.patchLoad(id, patch);
        return ResponseEntity.ok(loadService.convertToDTO(updated));
    }

    @GetMapping
    public List<Load> getAllLoads() {
        return loadService.getAllLoads();
    }

    @GetMapping("/{id}")
    public Load getLoadById(@PathVariable Long id) {
        return loadService.getLoadById(id);
    }

    @PatchMapping("/{id}/actual-weight")
    public ResponseEntity<LoadDTO> setActualWeight(@PathVariable Long id,
                                                   @Valid @RequestBody ActualWeightRequest request) {
        Load updated = loadService.setActualWeight(id, request.getActualWeight());
        return ResponseEntity.ok(loadService.convertToDTO(updated));
    }

    @GetMapping("/{id}/billable-amount")
    public ResponseEntity<LoadBillableAmountResponse> getBillableAmount(@PathVariable Long id) {
        return ResponseEntity.ok(loadService.getBillableAmount(id));
    }

    @DeleteMapping("/{id}")
    public void deleteLoad(@PathVariable Long id) {
        loadService.deleteLoad(id);
    }

    @PutMapping("/{id}/assign-driver")
    public ResponseEntity<LoadDTO> assignDriver(@PathVariable Long id,
                                                @RequestBody DriverAssignmentRequest request) {
        Load assigned = loadService.assignDriver(id, request);
        return ResponseEntity.ok(loadService.convertToDTO(assigned));
    }
}

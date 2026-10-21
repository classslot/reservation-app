package com.example.reservation.controller;

import com.example.reservation.dto.CommonDtos.PageResponse;
import com.example.reservation.dto.SlotDtos.*;
import com.example.reservation.service.SlotService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/slots")
public class SlotController {
    private final SlotService service;

    public SlotController(SlotService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<SlotResponse> list(@RequestParam(required = false) String type,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                           @RequestParam(required = false) String status,
                                           @RequestParam(defaultValue = "0") int page) {
        return service.list(type, date, status, page);
    }

    @GetMapping("/{id}")
    public SlotResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SlotResponse create(@Valid @RequestBody SlotCreateRequest request) {
        return service.create(request);
    }

    @PatchMapping("/{id}")
    public SlotResponse update(@PathVariable Long id, @Valid @RequestBody SlotUpdateRequest request) {
        return service.update(id, request);
    }
}

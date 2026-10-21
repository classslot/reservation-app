package com.example.reservation.controller;

import com.example.reservation.dto.CommonDtos.PageResponse;
import com.example.reservation.dto.StudentDtos.*;
import com.example.reservation.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** 生徒管理（スタッフ専用。権限は SecurityConfig で制御） */
@RestController
@RequestMapping("/api/students")
public class StudentController {
    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<StudentResponse> list(@RequestParam(required = false) String q,
                                              @RequestParam(defaultValue = "0") int page) {
        return service.list(q, page);
    }

    @GetMapping("/{id}")
    public StudentResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentResponse create(@Valid @RequestBody StudentCreateRequest request) {
        return service.create(request);
    }

    @PatchMapping("/{id}")
    public StudentResponse update(@PathVariable Long id, @Valid @RequestBody StudentUpdateRequest request) {
        return service.update(id, request);
    }
}

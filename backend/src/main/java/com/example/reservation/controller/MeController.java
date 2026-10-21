package com.example.reservation.controller;

import com.example.reservation.dto.CommonDtos.MeResponse;
import com.example.reservation.dto.CommonDtos.PageResponse;
import com.example.reservation.dto.ReservationDtos.ReservationResponse;
import com.example.reservation.service.AuthService;
import com.example.reservation.service.ReservationService;
import org.springframework.web.bind.annotation.*;

/** ログイン中の利用者本人に関するAPI */
@RestController
@RequestMapping("/api/me")
public class MeController {
    private final AuthService authService;
    private final ReservationService reservationService;

    public MeController(AuthService authService, ReservationService reservationService) {
        this.authService = authService;
        this.reservationService = reservationService;
    }

    @GetMapping
    public MeResponse me() {
        return authService.me();
    }

    @GetMapping("/reservations")
    public PageResponse<ReservationResponse> myReservations(@RequestParam(required = false) String status,
                                                            @RequestParam(defaultValue = "0") int page) {
        return reservationService.listMine(status, page);
    }

    @PostMapping("/reservations/{id}/cancel")
    public ReservationResponse cancel(@PathVariable Long id) {
        return reservationService.cancel(id, false);
    }
}

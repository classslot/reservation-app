package com.example.reservation.controller;

import com.example.reservation.dto.CommonDtos.PageResponse;
import com.example.reservation.dto.ReservationDtos.ReservationResponse;
import com.example.reservation.dto.ReservationDtos.ReserveRequest;
import com.example.reservation.service.ReservationService;
import com.example.reservation.service.ReservationService.ReserveResult;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    /** スタッフ用: 全予約・参加者の取得 */
    @GetMapping
    public PageResponse<ReservationResponse> list(@RequestParam(name = "slot_id", required = false) Long slotId,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(defaultValue = "0") int page) {
        return service.listAll(slotId, status, page);
    }

    /** 会員用: 予約確定（新規は201、取消済みの再予約は200） */
    @PostMapping
    public ResponseEntity<ReservationResponse> reserve(@Valid @RequestBody ReserveRequest request) {
        ReserveResult result = service.reserve(request.slotId());
        return ResponseEntity.status(result.created() ? 201 : 200).body(result.reservation());
    }

    /** スタッフ用: 予約取消 */
    @PostMapping("/{id}/cancel")
    public ReservationResponse cancel(@PathVariable Long id) {
        return service.cancel(id, true);
    }
}

package com.example.reservation.service;

import com.example.reservation.common.ApiException;
import com.example.reservation.common.Enums;
import com.example.reservation.domain.*;
import com.example.reservation.dto.CommonDtos.PageResponse;
import com.example.reservation.dto.ReservationDtos.*;
import com.example.reservation.repository.ReservationRepository;
import com.example.reservation.repository.SlotRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    static final int PAGE_SIZE = 20;

    /** created=true なら新規予約(201)、false なら取消済み予約の再予約(200) */
    public record ReserveResult(ReservationResponse reservation, boolean created) {}

    private final ReservationRepository reservations;
    private final SlotRepository slots;
    private final SlotService slotService;
    private final CurrentUserService current;
    private final Clock clock;

    public ReservationService(ReservationRepository reservations, SlotRepository slots, SlotService slotService,
                              CurrentUserService current, Clock clock) {
        this.reservations = reservations;
        this.slots = slots;
        this.slotService = slotService;
        this.current = current;
        this.clock = clock;
    }

    /**
     * 予約確定。枠を行ロックしたうえで 対象 → 受付状態 → 開始前 → 重複 → 満席 の順に検証し、
     * 同時予約でも定員を超えないようにする。
     */
    @Transactional
    public ReserveResult reserve(Long slotId) {
        Student student = current.student();
        Slot slot = slots.findByIdForUpdate(slotId).orElseThrow(() -> ApiException.notFound("予約枠が見つかりません"));
        LocalDateTime now = LocalDateTime.now(clock);

        if (!slotService.isTarget(slot, student)) throw ApiException.forbidden("この予約枠は予約対象外です");
        if (slot.getStatus() != SlotStatus.OPEN) throw ApiException.conflict("この予約枠は受付を停止しています");
        if (!slot.getStartAt().isAfter(now)) throw ApiException.conflict("開始済みの予約枠は予約できません");

        Reservation existing = reservations.findByStudent_IdAndSlot_Id(student.getId(), slotId).orElse(null);
        if (existing != null && existing.getStatus() == ReservationStatus.CONFIRMED) {
            throw ApiException.conflict("既にこの予約枠を予約しています");
        }
        long confirmed = reservations.countBySlot_IdAndStatus(slotId, ReservationStatus.CONFIRMED);
        if (confirmed >= slot.getCapacity()) throw ApiException.conflict("満席のため予約できません");

        if (existing != null) { // 取消済みの枠は同じ予約行を再利用する（student_id, slot_id は一意）
            existing.confirm(now);
            return new ReserveResult(toResponse(existing, false), false);
        }
        Reservation saved = reservations.save(new Reservation(student, slot, now));
        return new ReserveResult(toResponse(saved, false), true);
    }

    /** 取消。会員は自分の予約のみ。取消済みは何もせず成功、開始済みは不可 */
    @Transactional
    public ReservationResponse cancel(Long id, boolean asStaff) {
        Reservation r = reservations.findById(id).orElseThrow(() -> ApiException.notFound("予約が見つかりません"));
        if (!asStaff && !r.getStudent().getId().equals(current.student().getId())) {
            throw ApiException.forbidden("他の人の予約は操作できません");
        }
        if (r.getStatus() == ReservationStatus.CANCELLED) return toResponse(r, asStaff);
        LocalDateTime now = LocalDateTime.now(clock);
        if (!r.getSlot().getStartAt().isAfter(now)) throw ApiException.conflict("開始済みの予約は取り消せません");
        r.cancel(now);
        return toResponse(r, asStaff);
    }

    public PageResponse<ReservationResponse> listMine(String status, int page) {
        ReservationStatus st = Enums.parse(ReservationStatus.class, status, "状態");
        Long studentId = current.student().getId();
        Pageable pageable = PageRequest.of(Math.max(0, page), PAGE_SIZE,
                Sort.by(Sort.Order.desc("slot.startAt"), Sort.Order.desc("id")));
        Page<Reservation> result = st == null
                ? reservations.findByStudent_Id(studentId, pageable)
                : reservations.findByStudent_IdAndStatus(studentId, st, pageable);
        return PageResponse.of(result, result.getContent().stream().map(r -> toResponse(r, false)).toList());
    }

    /** スタッフ用。全予約と参加者を返す */
    public PageResponse<ReservationResponse> listAll(Long slotId, String status, int page) {
        ReservationStatus st = Enums.parse(ReservationStatus.class, status, "状態");
        Specification<Reservation> spec = (r, q, cb) -> cb.conjunction();
        if (slotId != null) spec = spec.and((r, q, cb) -> cb.equal(r.get("slot").get("id"), slotId));
        if (st != null) spec = spec.and((r, q, cb) -> cb.equal(r.get("status"), st));
        Pageable pageable = PageRequest.of(Math.max(0, page), PAGE_SIZE,
                Sort.by(Sort.Order.desc("slot.startAt"), Sort.Order.desc("id")));
        Page<Reservation> result = reservations.findAll(spec, pageable);
        return PageResponse.of(result, result.getContent().stream().map(r -> toResponse(r, true)).toList());
    }

    private ReservationResponse toResponse(Reservation r, boolean withStudent) {
        Slot s = r.getSlot();
        Student st = r.getStudent();
        return new ReservationResponse(r.getId(), r.getStatus(), r.getReservedAt(), r.getCancelledAt(),
                new SlotSummary(s.getId(), s.getType(), s.getTitle(), s.getTeacherName(), s.getStartAt(), s.getEndAt()),
                withStudent ? new StudentSummary(st.getId(), st.getName(), st.getCourse(), st.getGame(), st.getLevel()) : null);
    }
}

package com.example.reservation.service;

import com.example.reservation.common.ApiException;
import com.example.reservation.common.Enums;
import com.example.reservation.domain.*;
import com.example.reservation.dto.CommonDtos.PageResponse;
import com.example.reservation.dto.SlotDtos.*;
import com.example.reservation.repository.ReservationRepository;
import com.example.reservation.repository.SlotRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class SlotService {
    static final int PAGE_SIZE = 20;

    private final SlotRepository slots;
    private final ReservationRepository reservations;
    private final CurrentUserService current;
    private final Clock clock;

    public SlotService(SlotRepository slots, ReservationRepository reservations, CurrentUserService current, Clock clock) {
        this.slots = slots;
        this.reservations = reservations;
        this.current = current;
        this.clock = clock;
    }

    /** 会員には「受付中・開始前・自分が対象の枠」だけ、スタッフには全枠を返す */
    public PageResponse<SlotResponse> list(String type, LocalDate date, String status, int page) {
        SlotType slotType = Enums.parse(SlotType.class, type, "種別");
        SlotStatus slotStatus = Enums.parse(SlotStatus.class, status, "状態");
        Student student = current.user().getRole() == Role.MEMBER ? current.student() : null;

        Specification<Slot> spec = (r, q, cb) -> cb.conjunction();
        if (slotType != null) spec = spec.and((r, q, cb) -> cb.equal(r.get("type"), slotType));
        if (date != null) {
            LocalDateTime from = date.atStartOfDay();
            LocalDateTime to = date.plusDays(1).atStartOfDay();
            spec = spec.and((r, q, cb) -> cb.and(
                    cb.greaterThanOrEqualTo(r.<LocalDateTime>get("startAt"), from),
                    cb.lessThan(r.<LocalDateTime>get("startAt"), to)));
        }
        if (student == null) {
            if (slotStatus != null) spec = spec.and((r, q, cb) -> cb.equal(r.get("status"), slotStatus));
        } else {
            LocalDateTime now = LocalDateTime.now(clock);
            spec = spec.and((r, q, cb) -> cb.equal(r.get("status"), SlotStatus.OPEN))
                    .and((r, q, cb) -> cb.greaterThan(r.<LocalDateTime>get("startAt"), now))
                    .and(targetMatches("targetCourse", student.getCourse()))
                    .and(targetMatches("targetGame", student.getGame()))
                    .and(targetMatches("targetLevel", student.getLevel()));
            if (!student.isMakeupAllowed()) {
                spec = spec.and((r, q, cb) -> cb.notEqual(r.get("type"), SlotType.MAKEUP));
            }
        }
        Page<Slot> result = slots.findAll(spec,
                PageRequest.of(Math.max(0, page), PAGE_SIZE, Sort.by("startAt").and(Sort.by("id"))));
        return PageResponse.of(result, toResponses(result.getContent(), student));
    }

    public SlotResponse get(Long id) {
        Slot slot = slots.findById(id).orElseThrow(() -> ApiException.notFound("予約枠が見つかりません"));
        Student student = current.user().getRole() == Role.MEMBER ? current.student() : null;
        if (student != null && !isTarget(slot, student)) {
            throw ApiException.forbidden("この予約枠は対象外です");
        }
        return toResponses(List.of(slot), student).get(0);
    }

    @Transactional
    public SlotResponse create(SlotCreateRequest r) {
        String course = blankToNull(r.targetCourse());
        String game = blankToNull(r.targetGame());
        String level = blankToNull(r.targetLevel());
        validate(r.type(), r.startAt(), r.endAt(), course, game, level);
        Slot slot = slots.save(new Slot(r.type(), r.title().trim(), r.teacherName().trim(), r.startAt(), r.endAt(),
                r.capacity(), r.status() == null ? SlotStatus.OPEN : r.status(), course, game, level));
        return toResponses(List.of(slot), null).get(0);
    }

    /** 枠の更新・受付停止。予約済みの枠の日時変更や、確定予約数を下回る定員変更はできない */
    @Transactional
    public SlotResponse update(Long id, SlotUpdateRequest r) {
        Slot slot = slots.findByIdForUpdate(id).orElseThrow(() -> ApiException.notFound("予約枠が見つかりません"));
        long confirmed = reservations.countBySlot_IdAndStatus(id, ReservationStatus.CONFIRMED);

        if (r.type() != null) slot.setType(r.type());
        if (r.title() != null) slot.setTitle(required(r.title(), "タイトル"));
        if (r.teacherName() != null) slot.setTeacherName(required(r.teacherName(), "担当者"));

        boolean timeChanged = (r.startAt() != null && !r.startAt().equals(slot.getStartAt()))
                || (r.endAt() != null && !r.endAt().equals(slot.getEndAt()));
        if (timeChanged && confirmed > 0) {
            throw ApiException.conflict("予約済みの枠は日時を変更できません。先に予約を取り消してください");
        }
        if (r.startAt() != null) slot.setStartAt(r.startAt());
        if (r.endAt() != null) slot.setEndAt(r.endAt());

        if (r.capacity() != null) {
            if (r.capacity() < 1) throw ApiException.invalid("定員は1以上で入力してください");
            if (r.capacity() < confirmed) throw ApiException.conflict("定員を確定予約数より少なくできません");
            slot.setCapacity(r.capacity());
        }
        if (r.status() != null) slot.setStatus(r.status());
        if (r.targetCourse() != null) slot.setTargetCourse(blankToNull(r.targetCourse()));
        if (r.targetGame() != null) slot.setTargetGame(blankToNull(r.targetGame()));
        if (r.targetLevel() != null) slot.setTargetLevel(blankToNull(r.targetLevel()));

        validate(slot.getType(), slot.getStartAt(), slot.getEndAt(),
                slot.getTargetCourse(), slot.getTargetGame(), slot.getTargetLevel());
        return toResponses(List.of(slot), null).get(0);
    }

    /** 生徒がその枠の対象か（コース・ゲーム・レベル・振替許可）。未指定の条件は誰でもOK */
    public boolean isTarget(Slot slot, Student student) {
        if (slot.getType() == SlotType.MAKEUP && !student.isMakeupAllowed()) return false;
        return matches(slot.getTargetCourse(), student.getCourse())
                && matches(slot.getTargetGame(), student.getGame())
                && matches(slot.getTargetLevel(), student.getLevel());
    }

    /** 残席は保存せず、定員 − 確定予約数 で計算する（枠ごとにまとめて集計してN+1を避ける） */
    List<SlotResponse> toResponses(List<Slot> list, Student student) {
        if (list.isEmpty()) return List.of();
        List<Long> ids = list.stream().map(Slot::getId).toList();
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : reservations.countBySlotIds(ids, ReservationStatus.CONFIRMED)) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        Set<Long> mine = student == null ? Set.of()
                : new HashSet<>(reservations.findSlotIdsByStudent(student.getId(), ReservationStatus.CONFIRMED, ids));
        return list.stream().map(s -> {
            int remaining = (int) Math.max(0, s.getCapacity() - counts.getOrDefault(s.getId(), 0L));
            return new SlotResponse(s.getId(), s.getType(), s.getTitle(), s.getTeacherName(), s.getStartAt(), s.getEndAt(),
                    s.getTargetCourse(), s.getTargetGame(), s.getTargetLevel(), s.getCapacity(), remaining,
                    s.getStatus(), mine.contains(s.getId()));
        }).toList();
    }

    private static void validate(SlotType type, LocalDateTime start, LocalDateTime end,
                                 String course, String game, String level) {
        if (!start.isBefore(end)) throw ApiException.invalid("終了日時は開始日時より後にしてください");
        if (type == SlotType.MAKEUP && (course == null || game == null || level == null)) {
            throw ApiException.invalid("振替レッスンは対象コース・ゲーム・レベルを指定してください");
        }
    }

    private static Specification<Slot> targetMatches(String field, String value) {
        return (r, q, cb) -> cb.or(cb.isNull(r.get(field)), cb.equal(r.get(field), value));
    }

    private static boolean matches(String target, String actual) {
        return target == null || target.equals(actual);
    }

    private static String blankToNull(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }

    private static String required(String v, String label) {
        if (v.isBlank()) throw ApiException.invalid(label + "は空にできません");
        return v.trim();
    }
}

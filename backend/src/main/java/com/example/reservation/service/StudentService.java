package com.example.reservation.service;

import com.example.reservation.common.ApiException;
import com.example.reservation.domain.ReservationStatus;
import com.example.reservation.domain.Role;
import com.example.reservation.domain.Student;
import com.example.reservation.domain.User;
import com.example.reservation.dto.CommonDtos.PageResponse;
import com.example.reservation.dto.StudentDtos.*;
import com.example.reservation.repository.ReservationRepository;
import com.example.reservation.repository.StudentRepository;
import com.example.reservation.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class StudentService {
    static final int PAGE_SIZE = 20;

    private final StudentRepository students;
    private final UserRepository users;
    private final ReservationRepository reservations;
    private final PasswordEncoder encoder;
    private final Clock clock;

    public StudentService(StudentRepository students, UserRepository users, ReservationRepository reservations,
                          PasswordEncoder encoder, Clock clock) {
        this.students = students;
        this.users = users;
        this.reservations = reservations;
        this.encoder = encoder;
        this.clock = clock;
    }

    public PageResponse<StudentResponse> list(String q, int page) {
        Pageable pageable = PageRequest.of(Math.max(0, page), PAGE_SIZE, Sort.by("id"));
        Page<Student> result = (q == null || q.isBlank())
                ? students.findAll(pageable)
                : students.findByNameContaining(q.trim(), pageable);
        return PageResponse.of(result, result.getContent().stream().map(this::toResponse).toList());
    }

    public StudentResponse get(Long id) {
        return toResponse(find(id));
    }

    /** 生徒と会員アカウントを同じトランザクションで登録する */
    @Transactional
    public StudentResponse create(StudentCreateRequest r) {
        String loginId = r.loginId().trim();
        if (users.existsByLoginId(loginId)) {
            throw ApiException.conflict("このログインIDは既に使われています");
        }
        User user = users.save(new User(loginId, encoder.encode(r.password()), Role.MEMBER));
        Student student = students.save(new Student(user, r.name().trim(), r.guardianName().trim(),
                r.contactEmail().trim(), r.course().trim(), r.game().trim(), r.level().trim(),
                Boolean.TRUE.equals(r.makeupAllowed())));
        return toResponse(student);
    }

    /** 生徒情報の更新。退会（is_active=false）は履歴を残すため無効化で扱う */
    @Transactional
    public StudentResponse update(Long id, StudentUpdateRequest r) {
        Student s = find(id);
        if (r.name() != null) s.setName(required(r.name(), "氏名"));
        if (r.guardianName() != null) s.setGuardianName(required(r.guardianName(), "保護者氏名"));
        if (r.contactEmail() != null) s.setContactEmail(required(r.contactEmail(), "連絡先メールアドレス"));
        if (r.course() != null) s.setCourse(required(r.course(), "コース"));
        if (r.game() != null) s.setGame(required(r.game(), "ゲーム"));
        if (r.level() != null) s.setLevel(required(r.level(), "レベル"));
        if (r.makeupAllowed() != null) s.setMakeupAllowed(r.makeupAllowed());

        if (r.active() != null && r.active() != s.isActive()) {
            boolean hasFutureReservation = reservations.existsByStudent_IdAndStatusAndSlot_StartAtAfter(
                    id, ReservationStatus.CONFIRMED, LocalDateTime.now(clock));
            if (!r.active() && hasFutureReservation) {
                throw ApiException.conflict("今後の予約があるため退会できません。先に予約を取り消してください");
            }
            s.setActive(r.active());
            s.getUser().setActive(r.active()); // 退会するとログインもできなくなる
        }
        return toResponse(s);
    }

    public StudentResponse toResponse(Student s) {
        return new StudentResponse(s.getId(), s.getName(), s.getGuardianName(), s.getContactEmail(),
                s.getCourse(), s.getGame(), s.getLevel(), s.isMakeupAllowed(), s.isActive(), s.getUser().getLoginId());
    }

    private Student find(Long id) {
        return students.findById(id).orElseThrow(() -> ApiException.notFound("生徒が見つかりません"));
    }

    private static String required(String value, String label) {
        if (value.isBlank()) throw ApiException.invalid(label + "は空にできません");
        return value.trim();
    }
}

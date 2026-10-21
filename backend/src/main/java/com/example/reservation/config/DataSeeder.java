package com.example.reservation.config;

import com.example.reservation.domain.*;
import com.example.reservation.repository.ReservationRepository;
import com.example.reservation.repository.SlotRepository;
import com.example.reservation.repository.StudentRepository;
import com.example.reservation.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 動作確認用の初期データ（DBが空のときだけ登録）。
 *   スタッフ: staff   / staff1234
 *   会員    : member1 / member2 / member3  （パスワードはすべて pass1234）
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements ApplicationRunner {
    private final UserRepository users;
    private final StudentRepository students;
    private final SlotRepository slots;
    private final ReservationRepository reservations;
    private final PasswordEncoder encoder;
    private final Clock clock;

    public DataSeeder(UserRepository users, StudentRepository students, SlotRepository slots,
                      ReservationRepository reservations, PasswordEncoder encoder, Clock clock) {
        this.users = users;
        this.students = students;
        this.slots = slots;
        this.reservations = reservations;
        this.encoder = encoder;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.count() > 0) return;

        users.save(new User("staff", encoder.encode("staff1234"), Role.STAFF));
        Student s1 = member("member1", "山田 太郎", "山田 花子", "yamada@example.com", "ベーシック", "ゲームA", "初級", true);
        Student s2 = member("member2", "佐々木 次郎", "佐々木 恵", "sasaki@example.com", "アドバンス", "ゲームB", "中級", true);
        member("member3", "田中 三郎", "田中 美咲", "tanaka@example.com", "ベーシック", "ゲームA", "初級", false);

        LocalDate today = LocalDate.now(clock);
        slot(SlotType.MAKEUP, "振替レッスン（ゲームA 初級）", "John先生", today.plusDays(2), 16, 3, SlotStatus.OPEN, "ベーシック", "ゲームA", "初級");
        Slot second = slot(SlotType.MAKEUP, "振替レッスン（ゲームB 中級）", "David先生", today.plusDays(3), 17, 2, SlotStatus.OPEN, "アドバンス", "ゲームB", "中級");
        slot(SlotType.EVENT, "ゲームイベント", "Daniel先生", today.plusDays(5), 15, 10, SlotStatus.OPEN, null, null, null);
        slot(SlotType.MAKEUP, "振替レッスン（ゲームA 初級）", "John先生", today.plusDays(6), 16, 3, SlotStatus.CLOSED, "ベーシック", "ゲームA", "初級");

        reservations.save(new Reservation(s2, second, LocalDateTime.now(clock)));
    }

    private Student member(String loginId, String name, String guardian, String email,
                           String course, String game, String level, boolean makeupAllowed) {
        User user = users.save(new User(loginId, encoder.encode("pass1234"), Role.MEMBER));
        return students.save(new Student(user, name, guardian, email, course, game, level, makeupAllowed));
    }

    private Slot slot(SlotType type, String title, String teacher, LocalDate date, int hour, int capacity,
                      SlotStatus status, String course, String game, String level) {
        return slots.save(new Slot(type, title, teacher, date.atTime(hour, 0), date.atTime(hour + 1, 0),
                capacity, status, course, game, level));
    }
}

package com.example.reservation.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** slots（予約枠）: 振替レッスン／イベント。残席は保存せず計算する */
@Entity
@Table(name = "slots")
public class Slot extends BaseEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SlotType type;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "teacher_name", nullable = false, length = 100)
    private String teacherName;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "target_course", length = 50)
    private String targetCourse;

    @Column(name = "target_game", length = 50)
    private String targetGame;

    @Column(name = "target_level", length = 50)
    private String targetLevel;

    @Column(nullable = false)
    private int capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SlotStatus status = SlotStatus.OPEN;

    protected Slot() {}

    public Slot(SlotType type, String title, String teacherName, LocalDateTime startAt, LocalDateTime endAt,
                int capacity, SlotStatus status, String targetCourse, String targetGame, String targetLevel) {
        this.type = type;
        this.title = title;
        this.teacherName = teacherName;
        this.startAt = startAt;
        this.endAt = endAt;
        this.capacity = capacity;
        this.status = status;
        this.targetCourse = targetCourse;
        this.targetGame = targetGame;
        this.targetLevel = targetLevel;
    }

    public SlotType getType() { return type; }
    public void setType(SlotType type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
    public String getTargetCourse() { return targetCourse; }
    public void setTargetCourse(String targetCourse) { this.targetCourse = targetCourse; }
    public String getTargetGame() { return targetGame; }
    public void setTargetGame(String targetGame) { this.targetGame = targetGame; }
    public String getTargetLevel() { return targetLevel; }
    public void setTargetLevel(String targetLevel) { this.targetLevel = targetLevel; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public SlotStatus getStatus() { return status; }
    public void setStatus(SlotStatus status) { this.status = status; }
}

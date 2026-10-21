package com.example.reservation.domain;

import jakarta.persistence.*;

/** students（生徒）: 会員アカウント1件につき1名 */
@Entity
@Table(name = "students")
public class Student extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "guardian_name", nullable = false, length = 100)
    private String guardianName;

    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    @Column(nullable = false, length = 50)
    private String course;

    @Column(nullable = false, length = 50)
    private String game;

    @Column(nullable = false, length = 50)
    private String level;

    @Column(name = "makeup_allowed", nullable = false)
    private boolean makeupAllowed;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected Student() {}

    public Student(User user, String name, String guardianName, String contactEmail,
                   String course, String game, String level, boolean makeupAllowed) {
        this.user = user;
        this.name = name;
        this.guardianName = guardianName;
        this.contactEmail = contactEmail;
        this.course = course;
        this.game = game;
        this.level = level;
        this.makeupAllowed = makeupAllowed;
    }

    public User getUser() { return user; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGuardianName() { return guardianName; }
    public void setGuardianName(String guardianName) { this.guardianName = guardianName; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public boolean isMakeupAllowed() { return makeupAllowed; }
    public void setMakeupAllowed(boolean makeupAllowed) { this.makeupAllowed = makeupAllowed; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}

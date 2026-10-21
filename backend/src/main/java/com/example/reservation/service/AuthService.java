package com.example.reservation.service;

import com.example.reservation.domain.Role;
import com.example.reservation.domain.User;
import com.example.reservation.dto.CommonDtos.MeResponse;
import com.example.reservation.dto.CommonDtos.UserResponse;
import com.example.reservation.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {
    private final CurrentUserService current;
    private final StudentRepository students;
    private final StudentService studentService;

    public AuthService(CurrentUserService current, StudentRepository students, StudentService studentService) {
        this.current = current;
        this.students = students;
        this.studentService = studentService;
    }

    /** ログイン中の利用者情報。会員の場合は本人の生徒情報も返す（パスワードハッシュは返さない） */
    public MeResponse me() {
        User user = current.user();
        var student = user.getRole() == Role.MEMBER
                ? students.findByUser_Id(user.getId()).map(studentService::toResponse).orElse(null)
                : null;
        return new MeResponse(new UserResponse(user.getId(), user.getLoginId(), user.getRole(), user.isActive()), student);
    }
}

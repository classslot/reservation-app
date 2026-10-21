package com.example.reservation.service;

import com.example.reservation.common.ApiException;
import com.example.reservation.domain.Student;
import com.example.reservation.domain.User;
import com.example.reservation.repository.StudentRepository;
import com.example.reservation.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/** ログイン中の利用者（と本人の生徒情報）を取得する */
@Service
public class CurrentUserService {
    private final UserRepository users;
    private final StudentRepository students;

    public CurrentUserService(UserRepository users, StudentRepository students) {
        this.users = users;
        this.students = students;
    }

    public User user() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "ログインが必要です");
        }
        return users.findByLoginId(auth.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "ログインが必要です"));
    }

    public Student student() {
        User user = user();
        return students.findByUser_Id(user.getId())
                .orElseThrow(() -> ApiException.forbidden("生徒情報が登録されていません"));
    }
}

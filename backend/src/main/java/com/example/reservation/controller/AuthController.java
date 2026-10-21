package com.example.reservation.controller;

import com.example.reservation.dto.CommonDtos.LoginRequest;
import com.example.reservation.dto.CommonDtos.MeResponse;
import com.example.reservation.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final AuthService authService;

    public AuthController(AuthenticationManager authenticationManager,
                          SecurityContextRepository securityContextRepository, AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.authService = authService;
    }

    /** ログイン。認証に成功したらセッションに認証情報を保存する（失敗は401） */
    @PostMapping("/login")
    public MeResponse login(@Valid @RequestBody LoginRequest req, HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(req.loginId().trim(), req.password()));
        if (request.getSession(false) != null) {
            request.changeSessionId(); // セッション固定化攻撃の対策
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        return authService.me();
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
    }
}

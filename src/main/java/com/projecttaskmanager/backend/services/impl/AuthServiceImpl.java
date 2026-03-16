package com.projecttaskmanager.backend.services.impl;

import com.projecttaskmanager.backend.dto.request.auth.LoginRequest;
import com.projecttaskmanager.backend.dto.request.auth.RegisterRequest;
import com.projecttaskmanager.backend.dto.response.auth.AuthResponse;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.models.Role;
import com.projecttaskmanager.backend.models.User;
import com.projecttaskmanager.backend.repositories.RoleRepository;
import com.projecttaskmanager.backend.repositories.UserRepository;
import com.projecttaskmanager.backend.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new AppException(ErrorCode.FORBIDDEN));

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .isActive(true)
                .roles(Set.of(userRole))
//                .createdAt(Instant.now())
                .build();

        userRepository.save(user);

        return AuthResponse.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return AuthResponse.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .build();
    }
}
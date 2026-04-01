package com.projecttaskmanager.backend.services.impl;

import com.projecttaskmanager.backend.dto.request.auth.LoginRequest;
import com.projecttaskmanager.backend.dto.request.auth.RegisterRequest;
import com.projecttaskmanager.backend.dto.response.auth.AuthResponse;
import com.projecttaskmanager.backend.exceptions.AppException;
import com.projecttaskmanager.backend.exceptions.ErrorCode;
import com.projecttaskmanager.backend.models.EmailVerification;
import com.projecttaskmanager.backend.models.Role;
import com.projecttaskmanager.backend.models.User;
import com.projecttaskmanager.backend.repositories.EmailVerificationRepository;
import com.projecttaskmanager.backend.repositories.RoleRepository;
import com.projecttaskmanager.backend.repositories.UserRepository;
import com.projecttaskmanager.backend.services.AuthService;
import com.projecttaskmanager.backend.services.EmailService;
import com.projecttaskmanager.backend.services.JwtRedisService;
import com.projecttaskmanager.backend.services.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailService emailService;
    private final JwtRedisService jwtRedisService;

    @Value("${google.client-id}")
    private String googleClientId;

    private String generateOtp() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }

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

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        jwtRedisService.saveRefreshToken(
                user.getEmail(),
                refreshToken,
                1000L * 60 * 60 * 24 * 7
        );

        return AuthResponse.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        jwtRedisService.saveRefreshToken(
                user.getEmail(),
                refreshToken,
                1000L * 60 * 60 * 24 * 7
        );

        return AuthResponse.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthResponse loginWithGoogle(String idTokenString) {

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            )
                    .setAudience(List.of(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            String email = payload.getEmail();
            String fullName = (String) payload.get("name");

            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                Role userRole = roleRepository.findByName("USER")
                        .orElseThrow(() -> new AppException(ErrorCode.FORBIDDEN));

                user = User.builder()
                        .email(email)
                        .fullName(fullName)
                        .password("")
                        .isActive(true)
                        .roles(Set.of(userRole))
                        .build();

                userRepository.save(user);
            }

            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            jwtRedisService.saveRefreshToken(
                    user.getEmail(),
                    refreshToken,
                    1000L * 60 * 60 * 24 * 7
            );

            return AuthResponse.builder()
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

        } catch (Exception e) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }

    @Override
    public void sendOtp(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String otp = generateOtp();

        EmailVerification verification = EmailVerification.builder()
                .email(request.getEmail())
                .otp(otp)
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .expiryTime(Instant.now().plusSeconds(300))
                .verified(false)
                .build();

        emailVerificationRepository.save(verification);

        emailService.send(
                request.getEmail(),
                "Your OTP Code",
                "Your OTP is: " + otp
        );
    }

    @Override
    public AuthResponse verifyOtp(String email, String otp) {

        EmailVerification verification = emailVerificationRepository
                .findTopByEmailOrderByExpiryTimeDesc(email)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));

        if (!verification.getOtp().equals(otp)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (verification.getExpiryTime().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (userRepository.existsByEmail(email)) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new AppException(ErrorCode.FORBIDDEN));

        User user = User.builder()
                .email(verification.getEmail())
                .password(verification.getPassword())
                .fullName(verification.getFullName())
                .isActive(true)
                .roles(Set.of(userRole))
                .build();

        userRepository.save(user);

        emailVerificationRepository.delete(verification);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public void logout(String accessToken) {

        String email = jwtService.extractEmail(accessToken);
        long ttl = jwtService.getRemainingTime(accessToken);

        jwtRedisService.blacklistToken(accessToken, ttl);

        jwtRedisService.deleteRefreshToken(email);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {

        String email = jwtService.extractEmail(refreshToken);

        if (!jwtRedisService.isRefreshTokenValid(email, refreshToken)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtService.generateAccessToken(user);

        return AuthResponse.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
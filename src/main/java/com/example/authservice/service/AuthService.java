package com.example.authservice.service;

import com.example.authservice.dto.AuthResponse;
import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.RefreshRequest;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.entity.Role;
import com.example.authservice.entity.User;
import com.example.authservice.exception.EmailAlreadyExistsException;
import com.example.authservice.exception.InvalidCredentialsException;
import com.example.authservice.exception.InvalidRefreshTokenException;
import com.example.authservice.exception.RoleNotFoundException;
import com.example.authservice.repository.RoleRepository;
import com.example.authservice.repository.UserRepository;
import com.example.authservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email уже занят: " + request.getEmail());
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RoleNotFoundException("Роль ROLE_USER не найдена"));

        String passwordHash = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordHash);
        user.setEnabled(true);

        user.getRoles().add(userRole);

        userRepository.save(user);

        log.info("Зарегистрирован пользователь: {}", request.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        User user = userOptional.orElseThrow(() -> new InvalidCredentialsException("Неверный логин или пароль"));
        if (!user.isEnabled()) {
            throw new InvalidCredentialsException("Неверный логин или пароль");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Неверный логин или пароль");
        }
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user.getEmail());
        AuthResponse response = new AuthResponse(accessToken, refreshToken);
        return response;
    }

    public AuthResponse refresh(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        String username = refreshTokenService.getUsernameByToken(refreshToken)
                .orElseThrow(() -> new InvalidRefreshTokenException("Невалидный refresh token"));
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new InvalidRefreshTokenException("Невалидный refresh token"));
        String accessToken = jwtService.generateAccessToken(user);
        return new AuthResponse(accessToken, refreshToken);
    }

    public void logout(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        refreshTokenService.deleteRefreshToken(refreshToken);
        log.info("Пользователь вышел из системы");
    }
}

package com.eap08.domesticas.service;

import com.eap08.domesticas.dto.AuthResponse;
import com.eap08.domesticas.dto.RegisterRequest;
import com.eap08.domesticas.dto.LoginRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
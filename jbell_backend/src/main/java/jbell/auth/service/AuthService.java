package jbell.auth.service;

import jbell.auth.dto.SignupRequest;

public interface AuthService {
    void registerUser(SignupRequest request);
}
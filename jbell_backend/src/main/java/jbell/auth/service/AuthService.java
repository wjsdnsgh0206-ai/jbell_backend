package jbell.auth.service;

import jbell.auth.dto.SignupRequest;

public interface AuthService {
    void registerUser(SignupRequest request);
    
    // 아이디 중복 체크
    boolean isUserIdDuplicated(String userId);
}
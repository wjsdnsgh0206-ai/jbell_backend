package jbell.auth.service;

import java.util.Map;

import jbell.auth.dto.LoginRequest;
import jbell.auth.dto.SignupRequest;
import jbell.auth.dto.UserResponse;

public interface AuthService {
	void registerUser(SignupRequest request);
    boolean isUserIdDuplicated(String userId);
    
    Map<String, String> login(LoginRequest loginRequest); 
    
    String refreshAccessToken(String refreshToken);
    String findId(String name, String email);
    void resetPassword(String userId, String email);
    
    UserResponse getUserInfo(String userId);
}
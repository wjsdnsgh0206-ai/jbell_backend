package jbell.auth.service;

import java.util.List;
import java.util.Map;

import jbell.auth.dto.LoginRequest;
import jbell.auth.dto.SignupRequest;
import jbell.auth.dto.UserResponse;

public interface AuthService {
	void registerUser(SignupRequest request);
    boolean isUserIdDuplicated(String userId);
    
    Map<String, String> login(LoginRequest loginRequest); 
    
    String refreshAccessToken(String refreshToken);
    String findId(String email);
    public void resetPassword(String userId, String email, String newPw);
    
    UserResponse getUserInfo(String userId);
    void updateUserInfo(SignupRequest request); // 기존 SignupRequest를 재사용하거나 UpdateRequest를 새로 생성
    // 비밀번호 검증
    boolean checkPassword(String userId, String rawPassword);
    
    
    public Map<String, Object> getAdminMemberList(Map<String, Object> params);
    void deleteUsers(List<String> ids);
    
    public void updateUserByAdmin(jbell.auth.domain.User user);

}
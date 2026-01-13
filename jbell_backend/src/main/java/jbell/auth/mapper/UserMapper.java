package jbell.auth.mapper;

import org.apache.ibatis.annotations.Mapper; // 이 패키지 확인

import jbell.auth.dto.SignupRequest;

@Mapper // 👈 이 어노테이션이 반드시 있어야 합니다!
public interface UserMapper {
    int insertUser(SignupRequest signupRequest);
    int existsByUserId(String userId);
}
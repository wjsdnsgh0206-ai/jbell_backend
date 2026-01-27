package jbell.auth.mapper;

import org.apache.ibatis.annotations.Mapper; // 이 패키지 확인
import org.apache.ibatis.annotations.Param;

import jbell.auth.domain.User;
import jbell.auth.dto.SignupRequest;

@Mapper
public interface UserMapper {
	int insertUser(SignupRequest signupRequest);
    boolean existsByUserId(String userId);
    User findByUserId(String userId);
    
    

    // 리프레시 토큰 관련 메서드
    void updateRefreshToken(@Param("userId") String userId, @Param("refreshToken") String refreshToken);
    String getRefreshToken(String userId);

    // 아이디/비번 찾기 관련 메서드
    String findIdByNameAndEmail(@Param("name") String name, @Param("email") String email);
    void updatePassword(@Param("userId") String userId, @Param("newPw") String newPw);
}
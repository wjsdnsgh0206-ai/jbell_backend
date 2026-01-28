package jbell.auth.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper; // 이 패키지 확인
import org.apache.ibatis.annotations.Param;

import jbell.auth.domain.User;
import jbell.auth.dto.SignupRequest;

@Mapper
public interface UserMapper {
	int insertUser(SignupRequest signupRequest);
    boolean existsByUserId(String userId);
    User findByUserId(String userId);
    
    void updateUser(SignupRequest signupRequest);
    

    // 리프레시 토큰 관련 메서드
    void updateRefreshToken(@Param("userId") String userId, @Param("refreshToken") String refreshToken);
    String getRefreshToken(String userId);

    // 아이디/비번 찾기 관련 메서드
    String findIdByEmail(@Param("userEmail") String userEmail);
    void updatePassword(@Param("userId") String userId, @Param("newPw") String newPw);
    
    
    
    // 검색 조건 및 페이징을 포함한 목록 조회
    List<User> selectUserList(Map<String, Object> params);

    // 검색 조건에 맞는 전체 카운트 (페이징 계산용)
    int countUserList(Map<String, Object> params);

    // 선택된 ID 리스트 삭제
    void updateUsersStatus(@Param("ids") List<String> ids, @Param("status") Boolean status);
}
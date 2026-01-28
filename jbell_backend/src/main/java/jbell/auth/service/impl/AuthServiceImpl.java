package jbell.auth.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jbell.auth.domain.User;
import jbell.auth.dto.LoginRequest;
import jbell.auth.dto.SignupRequest;
import jbell.auth.dto.UserResponse;
import jbell.auth.mapper.UserMapper;
import jbell.auth.service.AuthService;
import jbell.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

	private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder; // SecurityConfig에서 등록한 빈 주입
    private final JwtTokenProvider jwtTokenProvider;
    
    
    // 관리자 수정
    @Override
    @Transactional
    public void updateUserByAdmin(jbell.auth.domain.User user) {
        // 1. 비밀번호 처리
        // 프론트에서 새 비밀번호를 입력했을 때만 암호화해서 저장
        if (user.getUserPw() != null && !user.getUserPw().trim().isEmpty()) {
            user.setUserPw(passwordEncoder.encode(user.getUserPw()));
        } else {
            // 입력이 없으면 null로 두어 MyBatis <if> 문에서 제외되게 함
            user.setUserPw(null);
        }

        // 2. Mapper 호출 (수정 실행)
        userMapper.updateUserByAdmin(user);
    }
    
    // 관리자용 회원 목록 조회
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAdminMemberList(Map<String, Object> params) {
        // String으로 넘어온 값을 안전하게 숫자로 변환
        int page = Integer.parseInt(String.valueOf(params.getOrDefault("page", "1")));
        int size = Integer.parseInt(String.valueOf(params.getOrDefault("size", "10")));
        
        // MyBatis 계산용 offset 설정
        params.put("offset", (page - 1) * size);
        params.put("limit", size);

        List<User> list = userMapper.selectUserList(params);
        int total = userMapper.countUserList(params);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return result;
    }

    @Override
    @Transactional
    public void deleteUsers(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        
        try {
            // 실제 삭제 대신 상태값을 false(0)로 업데이트하여 논리적 삭제 처리
            userMapper.updateUsersStatus(ids, false);
            log.info("관리자에 의한 회원 비활성화 완료. 대상: {}명", ids.size());
        } catch (Exception e) {
            log.error("회원 상태 업데이트 중 오류 발생: ", e);
            throw new RuntimeException("삭제 처리 중 오류가 발생했습니다.");
        }
    }

    
    // 회원정보 수정
    @Override
    @Transactional
    public void updateUserInfo(SignupRequest request) {
        // 비밀번호가 입력된 경우에만 암호화하여 업데이트 (이 로직 덕분에 DTO에서 필수를 빼도 안전합니다)
        if (request.getUserPw() != null && !request.getUserPw().isEmpty()) {
            request.setUserPw(passwordEncoder.encode(request.getUserPw()));
        }
        userMapper.updateUser(request);
    }

    // 로그인
    @Override
    public Map<String, String> login(LoginRequest loginRequest) {
        User user = userMapper.findByUserId(loginRequest.getUserId());
        
        if (user == null || !passwordEncoder.matches(loginRequest.getUserPw(), user.getUserPw())) {
        	log.info("{}", !passwordEncoder.matches(loginRequest.getUserPw(), user.getUserPw()));
            throw new RuntimeException("아이디 또는 비밀번호가 틀렸습니다.");
        }

        // [중요] 수정된 JwtTokenProvider에 따라 userGrade를 두 번째 인자로 전달합니다.
        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getUserGrade());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getUserId());

        userMapper.updateRefreshToken(user.getUserId(), refreshToken);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }
    
    // 비밀번호 검증
    @Override
    @Transactional(readOnly = true)
    public boolean checkPassword(String userId, String rawPassword) {
        // 1. 기존에 작성된 findByUserId를 사용하여 유저 정보 조회
        User user = userMapper.findByUserId(userId);
        
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        // 2. 입력된 비밀번호와 DB의 암호화된 비밀번호(userPw) 비교
        // matches(평문 비밀번호, 암호화된 비밀번호) 순서입니다.
        return passwordEncoder.matches(rawPassword, user.getUserPw());
    }

    // 리프레시 토큰으로 액세스 토큰 재발급
    @Override
    public String refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("리프레시 토큰이 만료되었습니다. 다시 로그인하세요.");
        }

        String userId = jwtTokenProvider.getUserId(refreshToken);
        String savedToken = userMapper.getRefreshToken(userId);

        if (!refreshToken.equals(savedToken)) {
            throw new RuntimeException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 재발급 시에도 권한 정보가 필요하므로 DB에서 유저 정보를 다시 조회합니다.
        User user = userMapper.findByUserId(userId);
        if (user == null) throw new RuntimeException("사용자를 찾을 수 없습니다.");

        //  두 개의 인자를 전달하도록 변경
        return jwtTokenProvider.createAccessToken(user.getUserId(), user.getUserGrade());
    }
    
	

    // 아이디 찾기
    @Override
    public String findId(String email) {
        String userId = userMapper.findIdByEmail(email);
        if (userId == null) throw new RuntimeException("일치하는 회원 정보가 없습니다.");
        return userId;
    }
    
    
    @Override
    @Transactional
    public void resetPassword(String userId, String email, String newPw) { // newPw 인자 추가
        User user = userMapper.findByUserId(userId);
        
        // 1. 유저 존재 여부 및 이메일 일치 확인
        if (user == null || !user.getUserEmail().equals(email)) {
            throw new RuntimeException("정보가 일치하지 않습니다.");
        }
        
        // 2. 프론트에서 넘어온 새 비밀번호 암호화
        String encodedPw = passwordEncoder.encode(newPw);
        
        // 3. DB 업데이트
        userMapper.updatePassword(userId, encodedPw);
        
        System.out.println("비밀번호 변경 완료: " + userId);
    }

    private UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .userEmail(user.getUserEmail())
                .userBirthDate(user.getUserBirthDate())
                .userGender(user.getUserGender())
                .userGrade(user.getUserGrade())
                .userResidenceArea(user.getUserResidenceArea())
                .createdAt(user.getCreatedAt())
                .status(user.getStatus())
                .build();
    }

    @Override
    @Transactional
    public void registerUser(SignupRequest request) {
        // 1. 비밀번호 암호화 (Salt는 내부에서 자동으로 처리됨)
        String encodedPassword = passwordEncoder.encode(request.getUserPw());
        
        // 2. 암호화된 비밀번호로 교체
        request.setUserPw(encodedPassword);

        // 3. DB 저장
        userMapper.insertUser(request);
    }
    
    
    @Override
    @Transactional(readOnly = true)
    public boolean isUserIdDuplicated(String userId) {
        // Mapper에서 개수를 조회하여 0보다 크면 중복으로 판단
        return userMapper.existsByUserId(userId);
    }
    
    
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserInfo(String userId) {
        User user = userMapper.findByUserId(userId);
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        return convertToResponse(user);
    }
}
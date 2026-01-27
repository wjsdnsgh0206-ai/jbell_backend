package jbell.auth.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jbell.auth.dto.LoginRequest;
import jbell.auth.dto.SignupRequest;
import jbell.auth.dto.UserResponse;
import jbell.auth.service.AuthService;
import jbell.auth.service.EmailService;
import jbell.common.response.ApiResponse;
import jbell.exception.BaseException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private EmailService emailService;

    // 1. 이메일 인증번호 발송
    @PostMapping("/email-send")
    public ResponseEntity<ApiResponse<String>> sendEmail(@RequestBody Map<String, String> request, HttpSession session) {
        try {
            String email = request.get("email");
            
            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body(ApiResponse.error(400, "이메일 주소가 누락되었습니다."));
            }

            String code = emailService.generateCode();
            
            session.setAttribute("emailCode", code);
            session.setAttribute("targetEmail", email);
            session.setMaxInactiveInterval(180); 
            
            emailService.sendVerificationMail(email, code);
            
            return ResponseEntity.ok(ApiResponse.success("인증번호가 전송되었습니다."));
        } catch (Exception e) {
            e.printStackTrace();
            // [수정된 부분] .body(...) 앞에 ResponseEntity.status(500)이 있어야 합니다.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(ApiResponse.error(500, "메일 발송에 실패했습니다."));
        }
    }

    // 2. 이메일 인증번호 검증
    @PostMapping("/email-verify")
    public ResponseEntity<ApiResponse<Boolean>> verifyEmail(@RequestBody Map<String, String> request, HttpSession session) {
        String userEmail = request.get("email");
        String inputCode = request.get("code");
        
        String sessionCode = (String) session.getAttribute("emailCode");
        String targetEmail = (String) session.getAttribute("targetEmail");

        // 세션에 저장된 정보와 사용자가 입력한 정보 비교
        if (sessionCode != null && sessionCode.equals(inputCode) && userEmail != null && userEmail.equals(targetEmail)) {
            // 인증 성공 시 세션에 상태 기록
            session.setAttribute("isEmailVerified", true);
            return ResponseEntity.ok(ApiResponse.success(true));
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body(ApiResponse.error(400, "인증번호가 틀렸거나 만료되었습니다.", false));
    }

    
    
    
    //토큰 연장(15분 이후)을 위한 API
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        String newAccessToken = authService.refreshAccessToken(refreshToken);
        
        Map<String, String> response = new HashMap<>();
        response.put("accessToken", newAccessToken);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Map<String, String> tokens = authService.login(loginRequest);
            return ResponseEntity.ok(ApiResponse.success(tokens));
            
        } catch (BaseException e) {
            return ResponseEntity.status(e.getErrorCode().status()).body(ApiResponse.error(e.getErrorCode().status().value(), e.getMessage()));
        } catch (RuntimeException e) { 
            // Service에서 던지는 RuntimeException("아이디 또는 비밀번호가 틀렸습니다")을 여기서 잡습니다.
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(401, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(500, "서버 내부 오류가 발생했습니다."));
        }
    }

    @GetMapping("/userinfo")
    public ResponseEntity<?> getUserInfo(@RequestParam String userId) {
        UserResponse userResponse = authService.getUserInfo(userId);
        return ResponseEntity.ok(ApiResponse.success(userResponse));
    }

    @GetMapping("/find-id")
    public ResponseEntity<?> findId(@RequestParam String name, @RequestParam String email) {
        String userId = authService.findId(name, email);
        return ResponseEntity.ok(ApiResponse.success(userId));
    }

    @PostMapping("/reset-pw")
    public ResponseEntity<?> resetPw(@RequestParam String userId, @RequestParam String email) {
        authService.resetPassword(userId, email);
        return ResponseEntity.ok(ApiResponse.success("임시 비밀번호가 발급되었습니다."));
    }
    
    
    

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) {
        authService.registerUser(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(ApiResponse.success(signupRequest));
    }
    
    
    
    
    @GetMapping("/checkid")
    public ResponseEntity<ApiResponse<Boolean>> checkUserId(@RequestParam("userId") String userId) {
        boolean isDuplicated = authService.isUserIdDuplicated(userId);
        
        if (isDuplicated) {
            // 이미 존재하는 경우: ERROR 응답 (409 상태코드와 메시지 전달)
            // 여기서 true는 "중복되었다"는 정보를 데이터 본문에 담는 의미입니다.
            return ResponseEntity.status(HttpStatus.CONFLICT)
                                 .body(ApiResponse.error(409, "이미 사용 중인 아이디입니다.", true));
        }
        
        // 사용 가능한 경우: SUCCESS 응답
        return ResponseEntity.ok(ApiResponse.success(false));
    }
}
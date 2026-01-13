package jbell.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jbell.auth.dto.SignupRequest;
import jbell.auth.service.AuthService;
import jbell.common.response.ApiResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) {
        authService.registerUser(signupRequest);
        
        // DTO의 필드명인 userId, name, email을 정확히 호출해야 함
        var responseData = SignupRequest.builder()
                            .userId(signupRequest.getUserId())
                            .name(signupRequest.getName())   // userName 아님
                            .email(signupRequest.getEmail()) // userEmail 아님
                            .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(ApiResponse.success(responseData));
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
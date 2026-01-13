package jbell.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
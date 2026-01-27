package jbell.auth.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private String userId;
    private String userName;
    private String userEmail;
    private String userBirthDate;
    private String userGender;
    private String userGrade;
    private String userResidenceArea; // 주소
    private LocalDateTime createdAt;   // 가입일
}

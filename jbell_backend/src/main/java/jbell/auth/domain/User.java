package jbell.auth.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter        // MyBatis가 데이터를 주입할 수 있도록 추가
@ToString
@Builder
@NoArgsConstructor  // 필수: MyBatis가 쿼리 결과를 담을 때 사용할 기본 생성자
@AllArgsConstructor // Builder 사용을 위해 유지
public class User {
    private String userId;
    private String userPw;
    private String userName;
    private String userGender;
    private String userBirthDate;
    private String userEmail;
    private LocalDateTime createdAt;
    
    
    @Builder.Default
    private String userGrade = "USER";

    private String userResidenceArea;
    private Boolean status;
    private LocalDateTime userCreatedAt;
    private LocalDateTime userUpdatedAt;
}
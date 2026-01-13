package jbell.auth.domain;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class User {
	private String userId;
	private String userPw;
    private String userName;
    private String userGender;
    private String userBirthDate;
    private String userEmail;
    private String userGrade;
    private String userResidenceArea;
    private Boolean status;
    private LocalDateTime userCreatedAt;
    private LocalDateTime userUpdatedAt;
}

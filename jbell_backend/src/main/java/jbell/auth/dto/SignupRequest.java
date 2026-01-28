package jbell.auth.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder // 이 어노테이션이 있어야 .builder()를 사용할 수 있습니다.
@AllArgsConstructor // Builder 사용 시 모든 필드를 포함하는 생성자가 필요합니다.
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SignupRequest {
	
	@NotBlank(message="회원아이디는 필수입력항목입니다")
	@Size(min=4, max=20, message = "회원아이디 최소4 최대20 글자이내로 입력해야합니다.")
	@Pattern(regexp = "^[a-zA-Z0-9]*$", message = "회원아이디는 영문자와 숫자만 입력가능합니다.")
	private String userId;
	
//	@NotBlank(message="회원비밀번호는 필수입력항목입니다")
//	@Size(min=4, max=20, message = "회원비밀번호는 최소4 최대20 글자이내로 입력해야합니다.")
	private String userPw;
	
	
	@NotBlank(message="회원이름은 필수입력항목입니다")
	private String userName;
	
	@NotNull(message="회원생년월일은 필수입력항목입니다") // LocalDate에는 NotNull을 써야 합니다.
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate userBirthDate;
	
	@NotBlank(message="회원이메일은 필수입력항목입니다")
	@Email(message="회원이메일을 올바르게 작성해주세요")
	private String userEmail;
	
	private String userResidenceArea;
	
	private String userGender;
	
	@Builder.Default
    private String userGrade = "USER";
	
	private Boolean userStatus;
	
	
		
}

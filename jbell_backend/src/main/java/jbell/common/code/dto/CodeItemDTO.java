package jbell.common.code.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class CodeItemDTO {
	
	@NotBlank(message = "그룹 코드는 필수입니다.")
    private String groupCode;
    private String groupName;    // ★ 추가: JOIN 결과인 그룹명을 담기 위함
    
	@NotBlank(message = "상세 코드 ID는 필수입니다.") // 리액트의 (필수)와 매칭
    private String subCode;
	
	@NotBlank(message = "상세 코드명은 필수입니다.") // 리액트의 (필수)와 매칭
    private String subName;
	
    private String desc;
    private Integer order;
    private boolean visible;
    private String createdAt;    // LocalDateTime -> String 변경
    private String updatedAt;    // LocalDateTime -> String 변경
}
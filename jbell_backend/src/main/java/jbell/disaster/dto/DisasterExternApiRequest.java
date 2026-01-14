package jbell.disaster.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DisasterExternApiRequest {
	/**
     * 한 페이지 결과 수
     */
    @NotNull(message = "한 페이지 결과 수는 필수입니다")
    @Min(value = 1, message = "한 페이지 결과 수는 1 이상이어야 합니다")
    private Integer numOfRows = 10;
    
    /**
     * 페이지 번호 (1 이상)
     */
    @NotNull(message = "페이지 번호는 필수입니다")
    @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다")
    private Integer pageNo = 1;
    
    /**
     * 응답 타입 (json 또는 xml만 허용)
     */
    @NotBlank(message = "응답 타입은 필수입니다")
    @Pattern(regexp = "^(json|xml)$", message = "응답 타입은 json 또는 xml만 가능합니다")
    private String type = "json";
    
    /**
     * 시군구
     */
    @NotBlank(message = "시군구는 필수입니다")
    private String sgg;
}

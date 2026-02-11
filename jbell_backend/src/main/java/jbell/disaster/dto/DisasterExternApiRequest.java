package jbell.disaster.dto;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

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
  //  @NotBlank(message = "시군구는 필수입니다")
    private String sgg;
    
    
    
    // 재난문자이력관리 =================
    // @NotNull(message = "조회시작일은 필수입니다") 
    @DateTimeFormat(pattern = "yyyy/MM/dd HH:mm:ss") // 이건 폼 데이터용
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd HH:mm:ss") // ⭐ 이게 JSON 파싱용 핵심!
    private LocalDateTime crtDt;

    // @NotBlank(message = "시도는 필수입니다")
    private String rgnNm;
    
    
    
    // 기상청 실시간 특보 =================
   // @NotNull(message = "조회 시작일자는 YYYYMMDD 형식이어야 합니다")
    @Pattern(regexp = "^\\d{8}$")
    private String inqDt;
    
}

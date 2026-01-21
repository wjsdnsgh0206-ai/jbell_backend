package jbell.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 정의되지 않은 필드가 와도 에러내지 않음
public class SafetyDataResponse {
    @JsonProperty("header")
    private Header header;
    
    @JsonProperty("body")
    private List<Map<String, Object>> body;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true) // 여기도 반드시 추가
    public static class Header {
        private String resultCode;
        private String resultMsg;
        private String errorMsg;    // 🚩 추가: 에러 발생 시 API 서버가 보내는 필드
        private Integer totalCount;
    }
}
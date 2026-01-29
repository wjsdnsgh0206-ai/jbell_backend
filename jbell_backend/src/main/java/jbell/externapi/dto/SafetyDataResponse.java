package jbell.externapi.dto;

import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SafetyDataResponse<T> {
    private Header header;
    private Integer numOfRows;
    private Integer pageNo;
    private Integer totalCount;
    private List<T> body; // ⭐ 핵심: items 없이 body가 바로 배열임!

    @Data
    public static class Header {
        private String resultCode;
        private String resultMsg;
        private String errorMsg;
    }
}
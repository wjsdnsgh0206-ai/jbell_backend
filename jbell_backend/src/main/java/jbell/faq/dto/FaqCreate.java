package jbell.faq.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class FaqCreate {
	
	private String faqCategory;
    private String faqTitle;
    
    // JSON 요청(Array)을 받는 필드
    private List<Map<String, Object>> faqContent;
    
    // DB 매핑(String)을 위한 필드 (요청 JSON에는 포함되지 않음)
    @JsonIgnore
    private String faqContentJson;

    private Integer faqDisplayOrder;
    private String faqVisibleYn;

}

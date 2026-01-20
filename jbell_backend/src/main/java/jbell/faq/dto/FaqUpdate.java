package jbell.faq.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class FaqUpdate {

	private Integer faqId;          // PK (수정 대상 식별)
    private String faqCategory;
    private String faqTitle;
    private List<Map<String, Object>> faqContent; // JSON 구조 데이터 수신
    private String faqContentJson;  // DB 저장용 변환된 JSON String
    private Integer faqDisplayOrder;
    private String faqVisibleYn;
}

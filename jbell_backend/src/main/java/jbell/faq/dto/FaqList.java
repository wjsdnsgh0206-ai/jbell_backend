package jbell.faq.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FaqList {

	private Long faqId;
    private String faqCategory;
    private String faqTitle;
    private Integer faqViewCount;
    private Integer faqDisplayOrder;
    private String faqVisibleYn;      // "Y" / "N"
    private LocalDateTime faqCreatedAt;
}

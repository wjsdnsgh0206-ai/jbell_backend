package jbell.faq.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FaqList {

	private Long faqId;
    private String faqCategory;
    private String faqTitle;
    private String faqContent;
    private String faqWrite;
    private Integer faqViewCount;
    private String faqVisibleYn;      // "Y" / "N"
    private LocalDateTime faqCreatedAt;
}

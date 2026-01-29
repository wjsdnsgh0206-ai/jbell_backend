package jbell.faq.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FaqDetail {

	private int faqId;
    private String faqCategory;
    private String faqTitle;
    private String faqContent;
    private String faqWrite;
    private Integer faqViewCount;
    private Integer faqDisplayOrder;
    private String faqVisibleYn;      // "Y" / "N"
    private LocalDateTime faqCreatedAt;
    private LocalDateTime faqUpdatedAt;
}

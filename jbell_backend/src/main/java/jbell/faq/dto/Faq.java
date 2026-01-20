package jbell.faq.dto;

import lombok.Data;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Faq {

	private int faqId;
    private String faqCategory;
    private String faqTitle;
    private String faqContent;
    private Integer faqViewCount;
    private Integer faqDisplayOrder;
    private String faqVisibleYn;
    private LocalDateTime faqCreatedAt;
		
}

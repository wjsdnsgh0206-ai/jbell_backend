package jbell.faq.dto;

import java.util.List;

import lombok.Data;

@Data
public class FaqBulkVisibility {
	
	private List<Integer> faqIds; // 변경할 FAQ ID 리스트
    private String visibleYn;     // 변경할 상태 값 ('Y' or 'N')

}

package jbell.faq.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jbell.common.response.ApiResponse;
import jbell.faq.dto.FaqDetail;
import jbell.faq.dto.FaqList;
import jbell.faq.service.FaqService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FaqUserRestController {
	
	private final FaqService faqservice;
	
	// 사용자용 FAQ 목록 조회 (공개된 것만 조회)
    @GetMapping("/faqlist") 
    public ResponseEntity<ApiResponse<List<FaqList>>> getFaqList() {
        List<FaqList> faqList = faqservice.getPublicFaqList();
        return ResponseEntity.ok(ApiResponse.success(faqList));
    }
    
    // 사용자용 FAQ 상세 조회
    @GetMapping("/faqdetail/{faqId}")
    public ResponseEntity<ApiResponse<FaqDetail>> getFaqDetail(@PathVariable("faqId") int faqId) {
        FaqDetail faqDetail = faqservice.getFaqDetail(faqId);
        return ResponseEntity.ok(ApiResponse.success(faqDetail));
    }

}

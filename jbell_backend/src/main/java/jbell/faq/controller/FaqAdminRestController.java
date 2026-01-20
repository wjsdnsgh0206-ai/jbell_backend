package jbell.faq.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jbell.common.response.ApiResponse; //
import jbell.faq.dto.FaqBulkDelete;
import jbell.faq.dto.FaqCreate; //
import jbell.faq.dto.FaqDetail;
import jbell.faq.dto.FaqList;
import jbell.faq.dto.FaqUpdate;
import jbell.faq.service.FaqService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class FaqAdminRestController {

    private final FaqService faqService;
    
    // FAQ 조회
    @GetMapping(value = {"/faqlist"})
	public ResponseEntity<ApiResponse<List<FaqList>>> getFaqList(){
		List<FaqList> faqList = faqService.getFaqList();
		return ResponseEntity.ok(ApiResponse.success(faqList));
	}
    
    // FAQ 상세 조회
    @GetMapping("/faqdetail/{faqId}")
    public ResponseEntity<ApiResponse<FaqDetail>> getFaqDetail(@PathVariable("faqId") int faqId) {
        FaqDetail faqDetail = faqService.getFaqDetail(faqId);
        return ResponseEntity.ok(ApiResponse.success(faqDetail));
    }
    // FAQ 등록
    @PostMapping("/faqadd")
    public ResponseEntity<ApiResponse<String>> createFaq(@RequestBody FaqCreate faqCreate) {
        faqService.createFaq(faqCreate);
        return ResponseEntity.ok(ApiResponse.success("FAQ registration successful")); 
    }
    // FAQ 정보 수정
    @PutMapping("/faqdetail/{faqId}")
    public ResponseEntity<ApiResponse<String>> updateFaq(
            @PathVariable("faqId") int faqId,
            @RequestBody FaqUpdate faqUpdate) {
        
        // PathVariable의 ID를 DTO에 설정 (데이터 불일치 방지)
        faqUpdate.setFaqId(faqId);
        
        faqService.updateFaq(faqUpdate);
        return ResponseEntity.ok(ApiResponse.success("FAQ update successful"));
    }
    
    // FAQ 삭제 (단일 및 일괄)
    @PostMapping("/faqdelete")
    public ResponseEntity<ApiResponse<String>> deleteFaq(@RequestBody FaqBulkDelete faqBulkDelete) {
        faqService.deleteFaq(faqBulkDelete.getFaqId());
        return ResponseEntity.ok(ApiResponse.success("FAQ deletion successful"));
    }
}
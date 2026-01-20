package jbell.faq.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jbell.exception.CustomException;
import jbell.exception.ErrorCode;
import jbell.faq.dto.FaqCreate;
import jbell.faq.dto.FaqDetail;
import jbell.faq.dto.FaqList;
import jbell.faq.dto.FaqUpdate;
import jbell.faq.mapper.FaqMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FaqService {

	private final FaqMapper faqMapper;
	private final ObjectMapper objectMapper; // Spring Boot 기본 빈 주입
	
	//FAQ목록 조회
	public List<FaqList> getFaqList(){
		return faqMapper.getFaqList();
	}
	
	// FAQ 상세 조회 (조회수 증가 + 예외 처리 포함)
	public FaqDetail getFaqDetail(int faqId) {
		// 1. 상세 데이터 조회
		FaqDetail faqDetail = faqMapper.getFaqDetail(faqId);
		
		// 2. 예외 처리: 데이터가 없는 경우 404 Not Found 발생
		if (faqDetail == null) {
			throw new CustomException(ErrorCode.NOT_FOUND);
		}
		
		// 3. 조회수 증가
		faqMapper.increaseViewCount(faqId);
        
        // 4. 반환 객체의 조회수 동기화 (DB 재조회 없이 메모리상에서 +1 처리)
        // DTO에 Setter나 @Data가 있으므로 값 변경 가능
        if (faqDetail.getFaqViewCount() != null) {
            faqDetail.setFaqViewCount(faqDetail.getFaqViewCount() + 1);
        } else {
            faqDetail.setFaqViewCount(1);
        }

		return faqDetail;
	}
	
	// FAQ 등록
    public void createFaq(FaqCreate faqCreate) {
    	try {
            // List<Map> -> JSON String 변환
            if (faqCreate.getFaqContent() != null) {
                String jsonString = objectMapper.writeValueAsString(faqCreate.getFaqContent());
                faqCreate.setFaqContentJson(jsonString); // DB용 필드에 설정
            }
        } catch (JsonProcessingException e) {
            log.error("FAQ Content JSON parsing error", e);
            throw new RuntimeException("FAQ 등록 중 데이터 변환 오류가 발생했습니다.");
        }
        
        faqMapper.insertFaq(faqCreate);
    }
    
    // FAQ 수정
    public void updateFaq(FaqUpdate faqUpdate) {
        try {
            // List<Map> -> JSON String 변환
            if (faqUpdate.getFaqContent() != null) {
                String jsonString = objectMapper.writeValueAsString(faqUpdate.getFaqContent());
                faqUpdate.setFaqContentJson(jsonString); // DB용 필드에 설정
            }
        } catch (JsonProcessingException e) {
            log.error("FAQ Content JSON parsing error", e);
            throw new RuntimeException("FAQ 수정 중 데이터 변환 오류가 발생했습니다.");
        }

        // 업데이트 실행
        int result = faqMapper.updateFaq(faqUpdate);

        // 예외 처리: 대상이 없어서 업데이트되지 않은 경우
        if (result == 0) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }
    }
    
    // FAQ 삭제 (단일 및 일괄)
    public void deleteFaq(List<Integer> faqIds) {
        // 삭제할 ID 리스트가 비어있는지 확인
        if (faqIds == null || faqIds.isEmpty()) {
            // 선택된 항목이 없는 경우 처리 (조용히 리턴하거나 예외 발생)
            return; 
        }
        
        // 삭제 실행
        faqMapper.deleteFaq(faqIds);
    }
}

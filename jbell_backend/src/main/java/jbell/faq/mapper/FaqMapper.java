package jbell.faq.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import jbell.faq.dto.FaqBulkVisibility;
import jbell.faq.dto.FaqCreate;
import jbell.faq.dto.FaqDetail;
import jbell.faq.dto.FaqList;
import jbell.faq.dto.FaqUpdate;



@Mapper
public interface FaqMapper {
	
	// FAQ 목록 조회
	List<FaqList> getFaqList();
	
	// FAQ 상세 조회
	FaqDetail getFaqDetail(int faqId);
	
	// FAQ 조회수 증가
    int increaseViewCount(int faqId);
	
	// FAQ 등록
    int insertFaq(FaqCreate faqCreate);
    
    // FAQ 수정
    int updateFaq(FaqUpdate faqUpdate);
    
    // FAQ 공개/비공개 일괄 변경
    int updateFaqVisibility(FaqBulkVisibility faqBulkVisibility);
    
    // FAQ 삭제
    int deleteFaq(List<Integer> faqIds);
    
    // FAQ 사용자용 목록 조회
    List<FaqList> getPublicFaqList();
}

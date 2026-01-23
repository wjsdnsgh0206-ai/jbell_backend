package jbell.qna.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jbell.qna.dto.QnaCreate;
import jbell.qna.dto.QnaDetail;
import jbell.qna.dto.QnaList;
import jbell.qna.dto.QnaUpdate;

@Mapper
public interface QnaMapper {
	
	/**
     * 관리자용 QnA 전체 목록 조회
     */
    List<QnaList> getQnaList();
    
    /**
     * QnA 상세 조회
     * - inquiry (문의 본문)
     * - code_item (카테고리명)
     * - inquiry_answer (답변 내용)
     * 위 3개 테이블을 조인하여 조회
     */
    QnaDetail getQnaDetail(@Param("qnaId") Long qnaId);
     
    /**
     * 답변 내역 삭제 (참조 무결성 유지용)
     * @param qnaIds 삭제할 문의 ID 리스트
     */
    void deleteInquiryAnswers(@Param("qnaIds") List<Long> qnaIds);

    /**
     * 문의 내역 삭제 (단건 및 다건)
     * @param qnaIds 삭제할 문의 ID 리스트
     */
    void deleteInquiries(@Param("qnaIds") List<Long> qnaIds);

}

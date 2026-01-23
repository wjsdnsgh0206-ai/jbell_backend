package jbell.qna.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jbell.qna.dto.QnaCreate;
import jbell.qna.dto.QnaDetail;
import jbell.qna.dto.QnaList;
import jbell.qna.dto.QnaUpdate;
import jbell.qna.mapper.QnaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class QnaService {
	
	private final QnaMapper qnaMapper;

	/**
     * QnA 전체 목록 조회 (관리자용)
     */
    @Transactional(readOnly = true)
    public List<QnaList> getQnaList() {
        return qnaMapper.getQnaList();
    }
    
    /**
     * QnA 상세 조회
     * @param qnaId 문의 ID
     */
    @Transactional(readOnly = true)
    public QnaDetail getQnaDetail(Long qnaId) {
        return qnaMapper.getQnaDetail(qnaId);
    }
    
    /**
     * QnA 삭제 (단건/다건 통합)
     * @param qnaIds 삭제할 문의 ID 목록
     * 로직: 참조 무결성 제약조건(FK)으로 인해 답변(inquiry_answer)을 먼저 삭제 후 문의(inquiry)를 삭제함
     */
    public void deleteQna(List<Long> qnaIds) {
        if (qnaIds == null || qnaIds.isEmpty()) {
            return; // 삭제할 대상이 없으면 종료
        }
        
        // 1. 해당 문의들에 달린 답변 내역 먼저 삭제 (FK 제약 위배 방지)
        qnaMapper.deleteInquiryAnswers(qnaIds);
        
        // 2. 문의 내역 삭제
        qnaMapper.deleteInquiries(qnaIds);
    }
	
}

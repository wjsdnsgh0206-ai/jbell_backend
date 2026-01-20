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
     * QnA 등록
     * @param qnaCreate 등록할 QnA 정보
     * 1. QnaMapper.insertQna 호출 시 '답변대기' 상태로 자동 저장됨
     */
    public void createQna(QnaCreate qnaCreate) {
        qnaMapper.insertQna(qnaCreate);
    }
    
    /**
     * QnA 수정
     * @param qnaId 대상 문의 ID (Long)
     * @param userId 요청자 ID (세션 값)
     * @param qnaUpdate 수정할 데이터
     */
    public void updateQna(Long qnaId, String userId, QnaUpdate qnaUpdate) {
        // 1. 기존 게시글 조회
        QnaDetail currentQna = qnaMapper.getQnaDetail(qnaId);
        
        if (currentQna == null) {
            throw new IllegalArgumentException("존재하지 않는 문의 내역입니다.");
        }

        // 2. 권한 검증: 로그인한 사용자가 실제 작성자인지 확인
        if (!currentQna.getUserId().equals(userId)) {
            throw new IllegalStateException("본인의 문의글만 수정할 수 있습니다.");
        }

        // 3. 상태별 수정 제한 로직
        String status = currentQna.getStatus();
        
        if ("답변완료".equals(status)) {
            // 답변 완료 시 데이터 보존을 위해 수정 불가
            throw new IllegalStateException("답변이 완료된 문의는 수정할 수 없습니다.");
        } 
        else if ("답변처리중".equals(status)) {
            // 답변 처리 중일 경우, 수정은 허용하되 로그 기록 (운영 정책 반영)
            log.info("관리자가 확인 중인 문의(ID: {})가 사용자에 의해 수정되었습니다.", qnaId);
        }

        // 4. 업데이트 수행
        qnaMapper.updateQna(qnaId, qnaUpdate);
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

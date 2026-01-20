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
     * QnA 전체 목록 조회 (본인 내역)
     * @param userId 사용자 ID
     */
    @Transactional(readOnly = true)
    public List<QnaList> getQnaList(String userId) {
        return qnaMapper.getQnaList(userId);
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
     * QnA 상세 조회
     * @param qnaId 문의 ID
     */
    @Transactional(readOnly = true)
    public QnaDetail getQnaDetail(long qnaId) {
        return qnaMapper.getQnaDetail(qnaId);
    }
    
    /**
     * QnA 수정 (보안 및 상태 체크 포함)
     * @param qnaId 수정할 문의 ID
     * @param userId 요청자 ID (세션에서 검증된 값)
     * @param qnaUpdate 수정할 정보
     */
    public void updateQna(long qnaId, String userId, QnaUpdate qnaUpdate) {
        // 1. 기존 게시글 조회
        QnaDetail currentQna = qnaMapper.getQnaDetail(qnaId);
        
        if (currentQna == null) {
            throw new IllegalArgumentException("존재하지 않는 문의 내역입니다.");
        }

        // 2. 권한 검증: 로그인한 사용자가 작성자인지 확인 
        if (!currentQna.getUserId().equals(userId)) {
            throw new IllegalStateException("본인의 문의글만 수정할 수 있습니다.");
        }

        // 3. 상태별 비즈니스 로직 적용 
        String status = currentQna.getStatus();
        
        if ("답변완료".equals(status)) {
            // 답변 완료: 데이터 무결성 보존을 위해 수정 절대 불가
            throw new IllegalStateException("답변이 완료되어 수정할 수 없습니다.");
        } 
        else if ("답변처리중".equals(status)) {
            // 답변 처리 중: 수정은 허용하되, 관리자 혼선 방지를 위해 로그 기록
            log.info("[QnA 수정 알림] 관리자가 확인 중인 문의글이 수정되었습니다. (ID: {}, User: {})", qnaId, userId);
        }
        // 답변대기: 자유롭게 수정 가능

        // 4. 업데이트 수행
        qnaMapper.updateQna(qnaId, qnaUpdate);
    }
	

	
}

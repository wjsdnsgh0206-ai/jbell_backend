package jbell.qna.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jbell.qna.dto.QnaAnswer;
import jbell.qna.dto.QnaAnswerCreate;
import jbell.qna.dto.QnaAnswerDetail;
import jbell.qna.dto.QnaAnswerUpdate;
import jbell.qna.mapper.QnaAnswerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly=true)
@Slf4j
public class QnaAnswerService {
	
	private final QnaAnswerMapper qnaAnswerMapper;

    /**
     * 답변 등록
     * 1. 답변 테이블 insert
     * 2. 문의 테이블 status -> '답변완료' 변경
     */
    @Transactional
    public Long createAnswer(QnaAnswerCreate createDto) {
        // 1:1 관계 검증 (이미 답변이 있는지 확인) 로직 필요 시 추가
        // if (qnaAnswerMapper.selectAnswerByQnaId(createDto.getQnaId()).isPresent()) {
        //     throw new IllegalStateException("이미 답변이 완료된 문의입니다.");
        // }

        QnaAnswer qnaAnswer = new QnaAnswer();
        qnaAnswer.setQnaId(createDto.getQnaId());
        qnaAnswer.setUserId(createDto.getUserId());
        qnaAnswer.setContent(createDto.getContent());

        qnaAnswerMapper.insertAnswer(qnaAnswer);

        // 상태 변경: 답변대기/처리중 -> 답변완료
        qnaAnswerMapper.updateInquiryStatus(createDto.getQnaId(), "답변완료");

        return qnaAnswer.getQnaAnswerId();
    }

    /**
     * 답변 조회
     */
    public QnaAnswerDetail getAnswer(Long qnaId) {
        return qnaAnswerMapper.selectAnswerByQnaId(qnaId)
                .orElseThrow(() -> new IllegalArgumentException("답변이 존재하지 않습니다."));
    }

    /**
     * 답변 수정
     */
    @Transactional
    public void updateAnswer(QnaAnswerUpdate updateDto) {
        QnaAnswer qnaAnswer = qnaAnswerMapper.selectAnswerById(updateDto.getQnaAnswerId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 답변입니다."));

        // 내용만 변경
        qnaAnswer.setContent(updateDto.getContent());
        qnaAnswerMapper.updateAnswer(qnaAnswer);
    }

    /**
     * 답변 삭제 (논리적 삭제)
     * 1. 답변 테이블 deleted_yn -> 'Y'
     * 2. 문의 테이블 status -> '답변대기' 변경
     */
    @Transactional
    public void deleteAnswer(Long qnaAnswerId) {
        QnaAnswer qnaAnswer = qnaAnswerMapper.selectAnswerById(qnaAnswerId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 답변입니다."));
        
        // 논리적 삭제 처리
        qnaAnswerMapper.deleteAnswer(qnaAnswerId);

        // 상태 변경: 답변완료 -> 답변대기 (원복)
        qnaAnswerMapper.updateInquiryStatus(qnaAnswer.getQnaId(), "답변대기");
    }

}

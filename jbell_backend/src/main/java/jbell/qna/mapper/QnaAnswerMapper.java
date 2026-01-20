package jbell.qna.mapper;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jbell.qna.dto.QnaAnswer;
import jbell.qna.dto.QnaAnswerDetail;

@Mapper
public interface QnaAnswerMapper {

	// 답변 등록
    int insertAnswer(QnaAnswer qnaAnswer);

    // 답변 조회 (1:1 관계이므로 qnaId로 조회, 삭제되지 않은 것만)
    Optional<QnaAnswerDetail> selectAnswerByQnaId(@Param("qnaId") Long qnaId);

    // 답변 단건 조회 (삭제 처리를 위한 존재 확인용)
    Optional<QnaAnswer> selectAnswerById(@Param("qnaAnswerId") Long qnaAnswerId);

    // 답변 수정
    int updateAnswer(QnaAnswer qnaAnswer);

    // 답변 논리적 삭제 (Soft Delete)
    int deleteAnswer(@Param("qnaAnswerId") Long qnaAnswerId);

    // 문의 테이블(inquiry)의 상태 변경 (답변대기 <-> 답변완료)
    int updateInquiryStatus(@Param("qnaId") Long qnaId, @Param("status") String status);

}

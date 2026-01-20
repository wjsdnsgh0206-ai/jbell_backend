package jbell.qna.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jbell.qna.dto.QnaCreate;
import jbell.qna.dto.QnaDetail;
import jbell.qna.dto.QnaList;

@Mapper
public interface QnaMapper {
	
	/**
     * QnA 목록 조회
     * @param userId 작성자 ID (보안 격리용)
     */
    List<QnaList> getQnaList(@Param("userId") String userId);
    
    /**
     * QnA 등록
     * 매핑 XML ID: insertQna
     */
    void insertQna(QnaCreate qnaCreate);
    
    /**
     * QnA 상세 조회
     * 매핑 XML ID: getQnaDetail
     */
    QnaDetail getQnaDetail(@Param("qnaId") int qnaId);
    
    /**
     * QnA 수정
     * @param qnaId 대상 문의 ID
     * @param update 수정할 데이터 객체
     */
    void updateQna(@Param("qnaId") int qnaId, @Param("update") jbell.qna.dto.QnaUpdate update);

}

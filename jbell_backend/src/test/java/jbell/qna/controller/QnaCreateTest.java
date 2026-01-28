package jbell.qna.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import jbell.qna.dto.QnaCreate;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 테스트 완료 후 DB 데이터를 롤백하여 초기 상태 유지
class QnaCreateTest {
	/**
	 * 테스트 코드 : QnA등록 테스트 코드.txt
	 */
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("QnA 등록 성공 테스트 - 세션 및 FK 참조 무결성 확인")
    void createQnaSuccess() throws Exception {
        // Given: 유효한 등록 데이터 생성
        QnaCreate requestDto = new QnaCreate();
        // [참고] code_item 테이블에 존재하는 실제 ID 사용 (ETC_INQ)
        requestDto.setQnaCategoryId("ETC_INQ"); 
        requestDto.setTitle("테스트 코드에서 생성한 문의 제목");
        requestDto.setContent("테스트 코드에서 생성한 문의 본문입니다.");
        requestDto.setIsVisible("Y");

        // When & Then: 세션에 userId를 담아 요청 전송
        mockMvc.perform(post("/api/admin/qnaadd")
                // [참고] user 테이블에 존재하는 실제 ID로 세션 시뮬레이션
                .sessionAttr("userId", "jbelltest") 
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk()) // 200 OK 기대
                .andDo(print());
    }

    @Test
    @DisplayName("QnA 등록 실패 테스트 - 비로그인(세션 없음) 요청")
    void createQnaFail_NoSession() throws Exception {
        // Given
        QnaCreate requestDto = new QnaCreate();
        requestDto.setQnaCategoryId("ETC_INQ");
        requestDto.setTitle("해킹 시도 제목");
        requestDto.setContent("로그인 안하고 글쓰기 시도");
        requestDto.setIsVisible("Y");

        // When & Then: 세션 정보 없이 요청 전송
        mockMvc.perform(post("/api/admin/qnaadd")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized()) // 401 Unauthorized 기대
                .andDo(print());
    }
}
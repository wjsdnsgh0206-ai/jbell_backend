package jbell;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import jbell.qna.dto.QnaUpdate;

@SpringBootTest
class JbellBackendApplicationTests {

	@Test
	void contextLoads() {
	}
	
	 /**
     * QnA 수정 API (보안 강화됨)
     * URL: PUT /api/user/qnaupdate?qnaId=1
     * * [보안 변경점]
     * userId를 파라미터로 받지 않고, 서버의 HttpSession에서 직접 추출합니다.
     * 이를 통해 타인의 ID로 위변조하여 요청하는 공격을 차단합니다.
     */
    @PutMapping("/qnaupdate")
    public ResponseEntity<Void> updateQna(
            @RequestParam("qnaId") int qnaId,
            @RequestBody QnaUpdate qnaUpdate,
            HttpSession session) {
        
        String sessionUserId = (String) session.getAttribute("userId");
        
        if (sessionUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            qnaService.updateQna(qnaId, sessionUserId, qnaUpdate);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            log.warn("QnA 수정 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

}

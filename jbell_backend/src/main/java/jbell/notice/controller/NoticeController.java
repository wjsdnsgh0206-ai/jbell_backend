package jbell.notice.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jbell.common.response.ApiResponse;
import jbell.notice.dto.NoticeDTO;
import jbell.notice.entity.Notice;
import jbell.notice.service.NoticeService;

// 공지사항 Controller

@RestController
@RequestMapping("/api/notice")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "https://www.jbell.com"
})
public class NoticeController {
	
	private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }
	
	@GetMapping
    public List<NoticeDTO> getNoticeDTOList() {
        return noticeService.getNoticeDTOList();
    }
	
	
    // 2. 공지사항 상세
    @GetMapping("/{id}")
    public NoticeDTO getNoticeDetail(@PathVariable("id") Long id) {
        return noticeService.getNoticeDetail(id);
    }

    // 등록
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createNotice(@RequestBody Notice notice) {
        noticeService.createNotice(notice);
        return ResponseEntity.ok(ApiResponse.success("등록 성공!"));
    }

    // 수정
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateNotice(
        @PathVariable("id") Long id,
        @RequestBody Notice notice
    ) {
        notice.setNoticeId(id);
        noticeService.updateNotice(notice);
        return ResponseEntity.ok(ApiResponse.success("수정 성공!"));
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteNotice(@PathVariable("id") Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.ok(ApiResponse.success("삭제 성공!"));
    }
}

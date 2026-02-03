package jbell.notice.controller;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbell.common.response.ApiResponse;
import jbell.notice.dto.NoticeDTO;
import jbell.notice.dto.NoticeFileDTO;
import jbell.notice.entity.Notice;
import jbell.notice.entity.NoticeFile;
import jbell.notice.service.NoticeFileService;
import jbell.notice.service.NoticeService;

@RestController
@RequestMapping("/api/notice")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "https://www.jbell.com"
})
public class NoticeController {
    
    private final NoticeService noticeService;
    private final NoticeFileService noticeFileService;

    public NoticeController(NoticeService noticeService, NoticeFileService noticeFileService) {
        this.noticeService = noticeService;
        this.noticeFileService = noticeFileService;
    }

    // 1. 공지사항 목록 (검색 및 타입 필터링)
    @GetMapping
    public List<NoticeDTO> getNoticeDTOList(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "contentType", required = false) String contentType) {
        return noticeService.getNoticeDTOList(keyword, contentType);
    }

    // 2. 공지사항 분류(타입) 목록 조회 - 추가됨
    @GetMapping("/types")
    public List<Map<String, Object>> getNoticeTypes() {
        return noticeService.getNoticeTypes();
    }
    
    // 3. 공지사항 상세
    @GetMapping("/{id}")
    public ResponseEntity<?> getNoticeDetail(
            @PathVariable("id") Long id,
            HttpServletRequest request,
            HttpServletResponse response) {
        NoticeDTO notice =
            noticeService.getNoticeDetailAndIncreaseViews(id, request, response);
        if (notice == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(notice);
    }

    

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> createNotice(
            @RequestPart("notice") Notice notice,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        try {
            noticeService.createNotice(notice, files);
            return ResponseEntity.ok(ApiResponse.success("등록 성공!"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(500, e.getMessage()));
        }
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<ApiResponse<?>> updateNotice(
            @PathVariable("id") Long id,
            @RequestPart("notice") Notice notice,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "deleteFileIds", required = false) List<Long> deleteFileIds
    ) {
        try {
            notice.setNoticeId(id);
            noticeService.updateNotice(notice, files, deleteFileIds);
            return ResponseEntity.ok(ApiResponse.success("수정 성공!"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(500, e.getMessage()));
        }
    }

    
    
    // 6. 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteNotice(@PathVariable("id") Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.ok(ApiResponse.success("삭제 성공!"));
    }

    
    
    // 7. 첨부파일 다운로드
    @GetMapping("/file/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(
        @PathVariable("fileId") Long fileId
    ) throws Exception {
        NoticeFileDTO file = noticeFileService.getFileById(fileId);
        Path path = Paths.get(file.getFilePath());
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" +
                URLEncoder.encode(file.getFileRealName(), StandardCharsets.UTF_8) +
                "\"")
            .body(resource);
    }
}
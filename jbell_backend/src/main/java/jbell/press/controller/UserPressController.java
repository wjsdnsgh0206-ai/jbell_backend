package jbell.press.controller;

import jbell.press.dto.PressDTO;
import jbell.press.service.PressService;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/press") // 사용자용 API 경로
public class UserPressController {

    private final PressService pressService;

    /**
     * 보도자료 목록 조회 (사용자용)
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getPressList(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(value = "roleType", defaultValue = "user") String roleType,
            @RequestParam(value = "searchCategory", required = false) String searchCategory,
            @RequestParam(value = "searchTerm", required = false) String searchTerm,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "visibleYn", required = false) String visibleYn) {
        
          Map<String, Object> result = pressService.getPressList(offset, limit, roleType, searchCategory, searchTerm, startDate, endDate, visibleYn);
          
        return ResponseEntity.ok(result);
    }

    /**
     * 보도자료 상세 조회 (사용자용)
     */
    @GetMapping("/{id}")
    public ResponseEntity<PressDTO> getPressDetail(@PathVariable("id") Long id) {
        PressDTO detail = pressService.getPressDetail(id);
        return ResponseEntity.ok(detail);
    }
    
    
    /**
     * 첨부파일 다운로드 API
     * @param fileName 서버에 저장된 실제 파일명 (UUID)
     * @param originName 사용자가 내려받을 때 보여줄 원래 파일명
     */
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String fileName,
            @RequestParam String originName) throws Exception {

        // 파일이 저장된 실제 경로 찾기
        Path filePath = Paths.get("C:/upload/press/").resolve(fileName);
        Resource resource = new UrlResource(filePath.toUri());

        // 파일이 존재하는지 확인
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        // 한글 파일명 깨짐 방지 설정
        String encodedOriginName = UriUtils.encode(originName, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedOriginName + "\"";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM) // 이진 파일 형식 설정
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }
}
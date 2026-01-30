package jbell.common.controller;

import jbell.common.domain.FileResultVO;
import jbell.common.response.ApiResponse;
import jbell.common.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    // 에디터용 이미지 업로드 (단건)
    @PostMapping("/upload/editor")
    public ApiResponse<FileResultVO> uploadEditorImage(@RequestParam("file") MultipartFile file) {
        FileResultVO result = fileService.saveFile(file, "INLINE");
        return ApiResponse.success(result);
    }
}
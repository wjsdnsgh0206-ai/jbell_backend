package jbell.common.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jbell.common.response.ApiResponse;
import jbell.common.utils.FilesUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/file")
@Slf4j
@RequiredArgsConstructor
public class FileController {

    private final FilesUtils filesUtils;

    // 에디터용 이미지 업로드 (단건)
    @PostMapping("/upload/editor")
    public ApiResponse<?> uploadEditorImage(@RequestParam("file") MultipartFile file) {
    	
    	var fileMetaData = filesUtils.uploadFile(file);
    	log.info("{}", fileMetaData);
        return ApiResponse.success(fileMetaData);
    }
}
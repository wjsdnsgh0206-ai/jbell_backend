package jbell.common.service;

import jbell.common.domain.AttachmentVO;
import jbell.common.domain.FileResultVO;
import jbell.common.mapper.AttachmentMapper;
import jbell.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final AttachmentMapper attachmentMapper;

    @Value("${file.upload-dir}") // 예: C:/jbell/uploads/
    private String uploadDir;

    @Value("${file.domain}")     // 예: http://localhost:8080/uploads/
    private String domain;

    /**
     * 물리 파일 저장 및 DB Insert
     */
    @Transactional
    public FileResultVO saveFile(MultipartFile file, String fileType) {
        if (file.isEmpty()) return null;

        try {
            // 1. 파일명 생성
            String originalName = file.getOriginalFilename();
            String ext = originalName.substring(originalName.lastIndexOf(".") + 1);
            String uuidName = UUID.randomUUID().toString() + "." + ext;

            // 2. 디스크 저장
            File dest = new File(uploadDir + uuidName);
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            file.transferTo(dest);

            // 3. DB 저장 (AttachmentVO)
            AttachmentVO attachment = new AttachmentVO();
            attachment.setFileExt(ext);
            attachment.setFilePath("/uploads/" + uuidName); // 웹 접근용 상대경로
            attachment.setFileName(uuidName);
            attachment.setFileRealName(originalName);
            attachment.setFileSize(file.getSize());
            attachment.setFileType(fileType); // "INLINE"

            attachmentMapper.insertAttachment(attachment);

            // 4. 결과 반환 (FileResultVO)
            return FileResultVO.builder()
                    .fileId(attachment.getFileId())
                    .fileUrl(domain + uuidName)
                    .originalName(originalName)
                    .build();

        } catch (IOException e) {
            log.error("파일 업로드 실패", e);
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.");
        }
    }

    /**
     * 게시글 저장 시 호출: 파일들에 content_id 매핑
     */
    @Transactional
    public void linkFilesToContent(Long contentId, List<Long> fileIds) {
        if (fileIds != null && !fileIds.isEmpty()) {
            attachmentMapper.updateContentId(contentId, fileIds);
        }
    }
}
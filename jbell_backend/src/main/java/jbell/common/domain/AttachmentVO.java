package jbell.common.domain;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AttachmentVO {
    private Long fileId;        // PK
    private String fileExt;     // 확장자 (png, jpg)
    private String filePath;    // 저장 경로 (웹 접근용 or 절대경로)
    private String fileName;    // UUID 저장명
    private String fileRealName;// 원본 파일명
    private Long fileSize;      // 파일 크기
    private Long contentId;     // 게시글 ID (FK, Nullable)
    private String fileType;    // 구분 (INLINE:본문용, ATTACH:첨부용)
    private LocalDateTime createdAt;
    
}
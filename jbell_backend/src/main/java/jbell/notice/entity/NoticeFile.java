package jbell.notice.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeFile {

    private Long fileId;        // file_id (PK)
    private Long contentId;     // content_id (FK, 공지사항 ID)
    private String fileExt;     // file_ext
    private String filePath;    // file_path
    private String fileName;    // file_name (저장된 파일명)
    private String fileRealName; // file_real_name (사용자가 올린 원본명)
    private Long fileSize;      // file_size
    private LocalDateTime createdAt; // created_at
}

package jbell.notice.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class NoticeFileDTO {

	private Long fileId;      // file_id와 매핑
    private String fileExt;    // file_ext와 매핑 (추가)
    private String filePath;   // file_path와 매핑 (추가)
    private String fileName;   // file_name(저장된 이름)과 매핑
    private String fileRealName; // file_real_name(원본 이름)과 매핑
    private Long fileSize;     // file_size와 매핑
    private Long contentId;    // content_id(공지사항 ID)와 매핑 (추가)
    private LocalDateTime createdAt;

}

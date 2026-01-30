package jbell.common.domain;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class FileResultVO {
    private Long fileId;        // DB 저장된 PK (나중에 글 저장할 때 필요)
    private String fileUrl;     // 에디터에 표시할 이미지 src URL
    private String originalName;
}
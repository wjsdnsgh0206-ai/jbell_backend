package jbell.notice.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class NoticeDTO {
    private Long id;
    private String title;
    private String content;
    private String author;
    private String isPublic;    // visible_yn 매핑 ('Y'/'N')
    private String contentType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<NoticeFileDTO> files;
    private Integer fileCount;
}

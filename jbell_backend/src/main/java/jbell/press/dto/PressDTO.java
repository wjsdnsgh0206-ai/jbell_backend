package jbell.press.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.*;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PressDTO {

    private Long contentId;

    private String title; // 제목

    private String body;  // 내용, 본문 (content)

    private String visibleYn; // 노출여부

    private String contentType;   // 'PR01'

    private String contentLink; // 출처 원본 링크

    private String userId; // 등록인 '관리자'

    private String regType; // 등록방식 '직접등록'

    private String source; // 출처 기관명

    private LocalDateTime createdAt; // 등록일시

    private LocalDateTime lastUpdateDate; // 수정일시
    
    private List<Map<String, Object>> fileList; // 첨부파일 목록을 담을 변수
   
    private List<Long> existingFileIds; // 수정 시 유지할 기존 첨부파일 ID 목록 
    
    private int fileCount;
    
    }
    
    



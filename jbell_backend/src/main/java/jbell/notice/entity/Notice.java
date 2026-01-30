package jbell.notice.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class Notice {
    private Long noticeId;      // content_id 매핑
    private String title;
    private String content;     // body 매핑
    private String author;      // user_id 매핑
    
    @JsonProperty("isPublic")
    private String visibleYn = "Y"; 
    private String contentType; 
    private int ordering = 0;
    private LocalDateTime createdAt;
}
package jbell.notice.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Notice {

    private Long noticeId;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    
    @JsonProperty("author")
    private String author;
    
    // JSON의 "isPublic" 필드를 이 변수에 매핑하겠다는 선언
    @JsonProperty("isPublic")
    private boolean isPublic; 
    

    public Notice() {}
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public boolean isPublic() { return isPublic; }
    public void setIsPublic(boolean isPublic) { this.isPublic = isPublic; }
    
    
    public Long getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(Long noticeId) {
        this.noticeId = noticeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

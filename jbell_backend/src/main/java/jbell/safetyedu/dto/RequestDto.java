package jbell.safetyedu.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class RequestDto {
    private String mgmtId;       
    private String regType;      
    private Integer orderNo;     
    private String title;        
    private String source;       
    private String sourceUrl;    
    private String summary;      
    private String footerNotice; 
    private String contact;      
    private Boolean isPublic;    
    
    // 작성자 ID (입력하지 않으면 서버에서 ADMIN 중 하나 자동 배정)
    private String userId; 

    private List<SectionDto> sections; 
    private List<LinkDto> links;       

    @Getter 
    @Setter 
    @NoArgsConstructor
    public static class SectionDto {
        private String id;
        private String subTitle;
        private List<ItemDto> items;
    }

    @Getter 
    @Setter 
    @NoArgsConstructor
    public static class ItemDto {
        private String id;
        private String type;
        private String text;
    }

    @Getter 
    @Setter 
    @NoArgsConstructor
    public static class LinkDto {
        private String id;
        private String label;
        private String url;
    }
}
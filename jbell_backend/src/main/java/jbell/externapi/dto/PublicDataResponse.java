package jbell.externapi.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 공공데이터포털 표준 응답 형식
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "response")
public class PublicDataResponse<T> {
    
	// response json 일때
    @JsonProperty("response")
	private Response<T> response;
    
    // response xml 일때
    @JacksonXmlProperty(localName = "header")
    private Header header;
    
    // response xml 일때
    @JacksonXmlProperty(localName = "body")
    private Body<T> body;
	
	@Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
	public static class Response<T> {
		// Header
	    @JsonProperty("header")
	    @JacksonXmlProperty(localName = "header")
	    private Header header;
	    
	    // Body
	    @JsonProperty("body")
	    @JacksonXmlProperty(localName = "body")
	    private Body<T> body;
	}
	
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        
        @JsonProperty("resultCode")
        @JacksonXmlProperty(localName = "resultCode")
        private String resultCode;
        
        @JsonProperty("resultMsg")
        @JacksonXmlProperty(localName = "resultMsg")
        private String resultMsg;
    }
    
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body<T> {
        
        @JsonProperty("items")
        @JacksonXmlProperty(localName = "items")
        private Items<T> items;
        
        @JsonProperty("numOfRows")
        @JacksonXmlProperty(localName = "numOfRows")
        private Integer numOfRows;
        
        @JsonProperty("pageNo")
        @JacksonXmlProperty(localName = "pageNo")
        private Integer pageNo;
        
        @JsonProperty("totalCount")
        @JacksonXmlProperty(localName = "totalCount")
        private Integer totalCount;
    }
    
    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items<T> {
        
        // JSON: "item" 배열
        // XML: <item> 태그들
        @JsonProperty("item")
        @JacksonXmlProperty(localName = "item")
        @JacksonXmlElementWrapper(useWrapping = false)  
        private List<T> item;
    }
}


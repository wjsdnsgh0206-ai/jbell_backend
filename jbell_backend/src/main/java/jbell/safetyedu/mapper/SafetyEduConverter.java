package jbell.safetyedu.mapper; // 편의상 mapper 패키지에 위치시키거나 converter 패키지 생성 가능

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jbell.safetyedu.domain.Content;
import jbell.safetyedu.dto.RequestDto;
import jbell.safetyedu.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SafetyEduConverter {

    private final ObjectMapper objectMapper;

    // DTO -> Entity (JSON 직렬화 포함)
    public void updateEntityFromDto(Content content, RequestDto dto) {
        content.setTitle(dto.getTitle());
        content.setOrdering(dto.getOrderNo());
        content.setContentLink(dto.getSourceUrl());
        content.setVisibleYn(Boolean.TRUE.equals(dto.getIsPublic()) ? "Y" : "N");

        try {
            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("mgmtId", dto.getMgmtId());
            bodyMap.put("regType", dto.getRegType());
            bodyMap.put("source", dto.getSource());
            bodyMap.put("summary", dto.getSummary());
            bodyMap.put("footerNotice", dto.getFooterNotice());
            bodyMap.put("contact", dto.getContact());
            bodyMap.put("sections", dto.getSections());
            bodyMap.put("links", dto.getLinks());

            String jsonBody = objectMapper.writeValueAsString(bodyMap);
            content.setBody(jsonBody);
        } catch (JsonProcessingException e) {
            log.error("JSON Serialization Error", e);
            throw new RuntimeException("데이터 변환 중 오류가 발생했습니다.", e);
        }
    }

    // Entity -> DTO (JSON 역직렬화 포함)
    public ResponseDto toResponseDto(Content content) {
        ResponseDto dto = new ResponseDto();
        dto.setId(content.getContentId());
        dto.setTitle(content.getTitle());
        dto.setOrderNo(content.getOrdering());
        dto.setSourceUrl(content.getContentLink());
        dto.setIsPublic("Y".equals(content.getVisibleYn()));
        String displayName = content.getUserName();
        if (!StringUtils.hasText(displayName)) {
            displayName = content.getUserId();
        }
        dto.setAuthor(content.getUserId());
        dto.setCreatedAt(content.getCreatedAt());
        dto.setUpdatedAt(content.getLastUpdateDate());

        if (StringUtils.hasText(content.getBody())) {
            try {
                Map<String, Object> bodyMap = objectMapper.readValue(
                        content.getBody(),
                        new TypeReference<Map<String, Object>>() {}
                );

                dto.setMgmtId((String) bodyMap.get("mgmtId"));
                dto.setRegType((String) bodyMap.get("regType"));
                dto.setSource((String) bodyMap.get("source"));
                dto.setSummary((String) bodyMap.get("summary"));
                dto.setFooterNotice((String) bodyMap.get("footerNotice"));
                dto.setContact((String) bodyMap.get("contact"));

                dto.setSections(objectMapper.convertValue(
                        bodyMap.get("sections"),
                        new TypeReference<List<ResponseDto.SectionDto>>() {}
                ));
                dto.setLinks(objectMapper.convertValue(
                        bodyMap.get("links"),
                        new TypeReference<List<ResponseDto.LinkDto>>() {}
                ));
            } catch (JsonProcessingException e) {
                log.error("JSON Deserialization Error ID: {}", content.getContentId(), e);
            }
        }
        return dto;
    }
}
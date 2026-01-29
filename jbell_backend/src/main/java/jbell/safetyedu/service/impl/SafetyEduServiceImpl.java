package jbell.safetyedu.service.impl;

import jbell.safetyedu.domain.Content;
import jbell.safetyedu.dto.RequestDto;
import jbell.safetyedu.dto.ResponseDto;
import jbell.safetyedu.mapper.SafetyEduConverter;
import jbell.safetyedu.mapper.SafetyEduMapper;
import jbell.safetyedu.service.SafetyEduService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SafetyEduServiceImpl implements SafetyEduService {

    private final SafetyEduMapper safetyEduMapper;
    private final SafetyEduConverter safetyEduConverter;

    private static final String CONTENT_TYPE = "SAFETY_EDU";

    @Override
    @Transactional(readOnly = true)
    public Page<ResponseDto> getSafetyEduList(Pageable pageable, String keyword, String searchType, String isPublic) {
        int offset = (int) pageable.getOffset();
        int limit = pageable.getPageSize();

        List<Content> contents = safetyEduMapper.selectSafetyEduList(keyword, searchType, isPublic, offset, limit);
        long total = safetyEduMapper.countSafetyEduList(keyword, searchType, isPublic);

        List<ResponseDto> dtos = contents.stream()
                .map(safetyEduConverter::toResponseDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, total);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseDto getSafetyEduDetail(Long id) {
        Content content = safetyEduMapper.selectContentById(id);
        if (content == null) {
            throw new RuntimeException("해당 교육 정보를 찾을 수 없습니다. ID: " + id);
        }
        return safetyEduConverter.toResponseDto(content);
    }

    @Override
    public Long createSafetyEdu(RequestDto dto) {
        Content content = new Content();
        content.setContentType(CONTENT_TYPE);

        // [변경] User ID 처리 로직
        // 1. DTO에 userId가 있으면 사용
        // 2. 없으면 DB에서 ADMIN 등급 사용자 하나 조회하여 할당
        String targetUserId = dto.getUserId();
        if (!StringUtils.hasText(targetUserId)) {
            targetUserId = safetyEduMapper.selectRandomAdminId();
            if (targetUserId == null) {
                // DB에 ADMIN 계정이 하나도 없는 경우 (예외처리)
                throw new RuntimeException("등록 가능한 관리자(ADMIN) 계정이 없습니다.");
            }
        }
        content.setUserId(targetUserId);

        safetyEduConverter.updateEntityFromDto(content, dto);
        safetyEduMapper.insertContent(content);
        return content.getContentId();
    }

    @Override
    public Long updateSafetyEdu(Long id, RequestDto dto) {
        Content content = safetyEduMapper.selectContentById(id);
        if (content == null) {
            throw new RuntimeException("수정할 데이터가 존재하지 않습니다. ID: " + id);
        }

        safetyEduConverter.updateEntityFromDto(content, dto);
        
        // 수정 시에는 작성자를 변경하지 않거나, 필요시 DTO 값으로 업데이트
        if (StringUtils.hasText(dto.getUserId())) {
             content.setUserId(dto.getUserId());
        }

        safetyEduMapper.updateContent(content);
        return content.getContentId();
    }

    @Override
    public void deleteSafetyEdu(Long id) {
        if (safetyEduMapper.selectContentById(id) == null) {
            throw new RuntimeException("삭제할 데이터가 존재하지 않습니다.");
        }
        safetyEduMapper.deleteContent(id);
    }

    @Override
    public void deleteSafetyEdus(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            safetyEduMapper.deleteContents(ids);
        }
    }

    @Override
    public void updateVisibility(List<Long> ids, Boolean isPublic) {
        if (ids != null && !ids.isEmpty()) {
            String visibleYn = Boolean.TRUE.equals(isPublic) ? "Y" : "N";
            safetyEduMapper.updateVisibility(ids, visibleYn);
        }
    }
}
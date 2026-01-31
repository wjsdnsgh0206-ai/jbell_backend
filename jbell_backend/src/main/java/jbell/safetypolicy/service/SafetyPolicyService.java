package jbell.safetypolicy.service;

import jbell.safetypolicy.mapper.SafetyPolicyMapper;
import jbell.safetypolicy.dto.SafetyPolicyDTO;
import jbell.common.response.PageResponse;
import jbell.exception.CustomException;
import jbell.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SafetyPolicyService {

    private final SafetyPolicyMapper safetyPolicyMapper;

    @Transactional(readOnly = true)
    public PageResponse<SafetyPolicyDTO> getSafetyPolicyList(int page, int size, String keyword, String visibleYn) {
        int offset = (page - 1) * size;
        
        // Mapper 메서드명도 일관성 있게 변경
        List<SafetyPolicyDTO> list = safetyPolicyMapper.selectSafetyPolicyList(offset, size, keyword, visibleYn);
        long total = safetyPolicyMapper.countSafetyPolicyList(keyword, visibleYn);
        
        return new PageResponse<>(list, total, page, size);
    }

    @Transactional(readOnly = true)
    public SafetyPolicyDTO getSafetyPolicyDetail(Long contentId) {
        SafetyPolicyDTO detail = safetyPolicyMapper.selectSafetyPolicyDetail(contentId);
        
        if (detail == null) {
            // DB에 데이터가 없는 경우이므로 POST_NOT_FOUND (404) 사용
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
        return detail;
    }
}
package jbell.safetypolicy.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jbell.common.response.PageResponse;
import jbell.exception.CustomException;
import jbell.exception.ErrorCode;
import jbell.safetypolicy.dto.SafetyPolicyDTO;
import jbell.common.service.FileService;
import jbell.safetypolicy.mapper.SafetyPolicyMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SafetyPolicyService {

    private final SafetyPolicyMapper safetyPolicyMapper;
    private final FileService fileService;

    /**
     * 안전정책 목록 조회
     */
    @Transactional(readOnly = true)
    public PageResponse<SafetyPolicyDTO> getSafetyPolicyList(int page, int size, String keyword, String visibleYn) {
        int offset = (page - 1) * size;
        List<SafetyPolicyDTO> list = safetyPolicyMapper.selectSafetyPolicyList(offset, size, keyword, visibleYn);
        long total = safetyPolicyMapper.countSafetyPolicyList(keyword, visibleYn);
        return new PageResponse<>(list, total, page, size);
    }
    
    /**
     * 안전정책 상세 조회
     */
    @Transactional(readOnly = true)
    public SafetyPolicyDTO getSafetyPolicyDetail(Long contentId) {
        SafetyPolicyDTO detail = safetyPolicyMapper.selectSafetyPolicyDetail(contentId);
        if (detail == null) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }
        return detail;
    }
    
    /**
     * 안전정책 등록
     */
    @Transactional
    public Long createSafetyPolicy(SafetyPolicyDTO dto) {
        // 1. 기본값 설정
        if (dto.getVisibleYn() == null) dto.setVisibleYn("Y");
        
        // 2. 게시글 DB 저장 (여기서 contentId가 생성됨 keyProperty="contentId")
        safetyPolicyMapper.insertSafetyPolicy(dto);
        
        // 3. [핵심] 파일 매핑 로직 호출
        // DTO에 담겨온 fileIds(임시 저장된 파일들의 PK)를 현재 생성된 contentId와 연결합니다.
        if (dto.getFileIds() != null && !dto.getFileIds().isEmpty()) {
             fileService.linkFilesToContent(dto.getContentId(), dto.getFileIds());
        }

        return dto.getContentId();
    }

    /**
     * 안전정책 수정
     */
    @Transactional
    public void updateSafetyPolicy(SafetyPolicyDTO dto) {
        // 1. 게시글 내용 수정
        int result = safetyPolicyMapper.updateSafetyPolicy(dto);
        
        if (result == 0) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }

        // 2. [핵심] 파일 매핑 로직 호출 (새로 추가된 이미지가 있을 경우 연결)
        // 기존에 이미 연결된 파일은 update 해도 content_id가 같으므로 문제 없음
        if (dto.getFileIds() != null && !dto.getFileIds().isEmpty()) {
            fileService.linkFilesToContent(dto.getContentId(), dto.getFileIds());
        }
    }

    /**
     * 일괄 처리 로직
     */
    @Transactional
    public void updateVisibility(List<Long> ids, String visibleYn) {
        if (ids == null || ids.isEmpty()) return;
        safetyPolicyMapper.updateVisibility(ids, visibleYn);
    }

    @Transactional
    public void deleteSafetyPolicies(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        safetyPolicyMapper.deleteSafetyPolicies(ids);
    }
}
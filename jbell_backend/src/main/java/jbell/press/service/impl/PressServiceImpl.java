package jbell.press.service.impl;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jbell.common.domain.AttachmentVO;
import jbell.common.domain.FileMetaData;
import jbell.common.utils.FilesUtils;
import jbell.press.dto.PressDTO;
import jbell.press.mapper.PressMapper;
import jbell.press.service.PressService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PressServiceImpl implements PressService {

    private final PressMapper pressMapper;
    private final FilesUtils filesUtils;
    @Value("${file.path}")
    private final String uploadPath = "C:/upload/press/";

    @Override
    @Transactional
    public Long savePress(PressDTO dto, List<MultipartFile> files) throws Exception {

        dto.setContentType("PR01");
        dto.setRegType("직접등록");
        
        // 글 등록
        pressMapper.insertContent(dto);
        Long contentId = dto.getContentId();
        
        // 첨부파일이 존재할 경우 등록
        if(files != null && files.size() > 0) {
        	List<FileMetaData> fileList = filesUtils.uploadFiles(files);
        	fileList.forEach(f -> f.setFileIdx(contentId));
        	var uploadFileList = fileList.stream()
        			.map(FileMetaData :: toAttachmentVO)
        			.collect(Collectors.toList());
        	
        	pressMapper.insertAttachmentList(uploadFileList);
        }
        
        return contentId;
    }

    @Override
    public Map<String, Object> getPressList(int offset, int limit, String roleType, String searchCategory, String searchTerm, String startDate, String endDate, String visibleYn) {
        // 목록 조회
        List<PressDTO> list = pressMapper.getPressList(offset, limit, roleType, searchCategory, searchTerm, startDate, endDate, visibleYn);

        // 전체 개수 조회
        int totalCount = pressMapper.getPressListCount(roleType, searchCategory, searchTerm, startDate, endDate, visibleYn);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("totalCount", totalCount);
        
        return result;
    }

    @Override
    public PressDTO getPressDetail(Long contentId) {
        // 게시글 정보
        PressDTO detail = pressMapper.getPressById(contentId);
        

        if (detail != null) {
            List<Map<String, Object>> files = pressMapper.getFileList(contentId);
            detail.setFileList(files);
        }
        
        return detail;
    }

    @Override
    @Transactional
    public void deletePress(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            // 자식 테이블(attachment) 데이터부터 먼저 삭제
            for (Long id : ids) {
                pressMapper.deleteAttachmentsByContentId(id);
            }
            // 부모 테이블(content) 데이터 삭제
            pressMapper.deletePress(ids);
        }
    }
    
    @Override
    @Transactional
    public void updateVisibleStatus(List<Long> ids, String visibleYn) {
    	// 노출 비노출
        if (ids != null && !ids.isEmpty()) {
            pressMapper.updateVisibleStatus(ids, visibleYn);
        }
    }

    @Override
    @Transactional
    public void updatePress(PressDTO dto, List<MultipartFile> files) throws Exception {
        // 게시글 본문 텍스트 정보 업데이트
        pressMapper.updateContent(dto);

        // 파일 정리: 기존 파일 중 유지할 목록(existingFileIds) 제외하고 삭제
        Map<String, Object> params = new HashMap<>();
        params.put("contentId", dto.getContentId());
        params.put("existingIds", dto.getExistingFileIds());
        
        pressMapper.deleteAttachmentsExcludeIds(params);

        // 신규 추가된 파일 업로드 및 DB 등록
        if (files != null && !files.isEmpty()) {
            List<FileMetaData> fileList = filesUtils.uploadFiles(files);
            fileList.forEach(f -> f.setFileIdx(dto.getContentId()));
            
            var uploadFileList = fileList.stream()
                                         .map(FileMetaData::toAttachmentVO)
                                         .collect(Collectors.toList());
            
            pressMapper.insertAttachmentList(uploadFileList);
        }
    }
    
    
    
    
}

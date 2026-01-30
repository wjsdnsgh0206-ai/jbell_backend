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
        // 1. 기본값 세팅
        dto.setContentType("PR01");
        dto.setRegType("직접등록");
        
        // 2. 글 등록
        pressMapper.insertContent(dto);
        Long contentId = dto.getContentId();
        
        List<FileMetaData> fileList = filesUtils.uploadFiles(files);
        fileList.forEach(f -> f.setFileIdx(contentId));
        var uploadFileList = fileList.stream()
				        			  .map(FileMetaData :: toAttachmentVO)
				        			  .collect(Collectors.toList());
        
        pressMapper.insertAttachmentList(uploadFileList);
        
        return contentId;
    }

    @Override
    public List<PressDTO> getPressList(int offset, int limit) {
        return pressMapper.getPressList(offset, limit);
    }

    @Override
    public PressDTO getPressDetail(Long contentId) {
        // 1. 게시글 정보 가져오기
        PressDTO detail = pressMapper.getPressById(contentId);
        
        // 2. 해당 게시글에 딸린 파일 목록 가져와서 DTO에 쏙 넣기
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
            pressMapper.deletePress(ids);
        }
    }
    
    @Override
    @Transactional
    public void updatePress(PressDTO dto, List<MultipartFile> files) throws Exception {
        // 1. 게시글 텍스트 정보 업데이트
        pressMapper.updateContent(dto);

        // 2. 파일 정리: 유지 목록(existingFileIds)에 없는 파일 삭제
        // 만약 existingFileIds가 비어있다면 해당 게시글의 모든 파일 삭제
        pressMapper.deleteAttachmentsExcludeIds(dto.getContentId(), dto.getExistingFileIds());
        if(files != null && !files.isEmpty()) {        	
        	List<FileMetaData> fileList = filesUtils.uploadFiles(files);
        	fileList.forEach(f -> f.setFileIdx(dto.getContentId()));
        	var uploadFileList = fileList.stream()
					        		     .map(FileMetaData :: toAttachmentVO)
					        			 .collect(Collectors.toList());
        	pressMapper.insertAttachmentList(uploadFileList);
        }
        
    }
    
    
}

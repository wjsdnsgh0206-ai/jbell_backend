package jbell.press.service;

import jbell.press.dto.PressDTO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface PressService {
	
    // 보도자료 등록
    Long savePress(PressDTO dto, List<MultipartFile> files) throws Exception;
    
    // 목록 조회
    List<PressDTO> getPressList(int offset, int limit, String roleType, String searchCategory, String searchTerm);
    
    // 상세 조회
    PressDTO getPressDetail(Long contentId);
    
    // 수정
    void updatePress(PressDTO dto, List<MultipartFile> files) throws Exception;
    
    // 삭제
    void deletePress(List<Long> ids);
}
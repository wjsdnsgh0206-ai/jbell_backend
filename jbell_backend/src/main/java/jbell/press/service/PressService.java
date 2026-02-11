package jbell.press.service;

import jbell.press.dto.PressDTO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

public interface PressService {
	
    // 보도자료 등록
    Long savePress(PressDTO dto, List<MultipartFile> files) throws Exception;
    
    // 목록 조회
    Map<String, Object> getPressList(int offset, int limit, String roleType, String searchCategory, String searchTerm, String startDate, String endDate, String visibleYn);

    // List<PressDTO> getPressList(int offset, int limit, String roleType, String searchCategory, String searchTerm);
    
    // 상세 조회
    PressDTO getPressDetail(Long contentId);
    
    // 노출 비노출
    void updateVisibleStatus(List<Long> ids, String visibleYn);
    
    // 수정
    void updatePress(PressDTO dto, List<MultipartFile> files) throws Exception;
    
    // 삭제
    void deletePress(List<Long> ids);
}
package jbell.safetyedu.service;

import jbell.safetyedu.dto.RequestDto;
import jbell.safetyedu.dto.ResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface SafetyEduService {
	
    Page<ResponseDto> getSafetyEduList(Pageable pageable, String keyword, String searchType, String isPublic);
    
    ResponseDto getSafetyEduDetail(Long id);
    
    Long createSafetyEdu(RequestDto requestDto);
    
    Long updateSafetyEdu(Long id, RequestDto requestDto);
    
    void deleteSafetyEdu(Long id);
    
    void deleteSafetyEdus(List<Long> ids);
    
    void updateVisibility(List<Long> ids, Boolean isPublic);
    
}
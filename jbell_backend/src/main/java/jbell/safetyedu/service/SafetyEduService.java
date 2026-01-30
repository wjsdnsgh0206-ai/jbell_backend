package jbell.safetyedu.service;

import jbell.safetyedu.dto.RequestDto;
import jbell.safetyedu.dto.ResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * 안전 교육(SafetyEdu) 관련 비즈니스 로직을 처리하는 서비스 인터페이스입니다.
 */
public interface SafetyEduService {
	
	/**
     * 조건에 맞는 안전 교육 목록을 페이징하여 조회합니다.
     *
     * @param pageable   페이징 정보 (페이지 번호, 크기, 정렬 방식)
     * @param keyword    검색 키워드 (제목, 내용 등)
     * @param searchType 검색 조건 유형
     * @param isPublic   공개 여부 필터 (문자열 형태)
     * @return 조회된 안전 교육 목록(Page 객체)
     */
    Page<ResponseDto> getSafetyEduList(Pageable pageable, String keyword, String searchType, String isPublic);
    
    /**
     * 특정 ID를 가진 안전 교육의 상세 정보를 조회합니다.
     *
     * @param id 조회할 안전 교육의 고유 ID
     * @return 상세 정보가 담긴 DTO 객체
     */
    ResponseDto getSafetyEduDetail(Long id);
    
    /**
     * 신규 안전 교육 정보를 생성합니다.
     *
     * @param requestDto 생성할 안전 교육 정보가 담긴 요청 객체
     * @return 생성된 안전 교육의 고유 ID
     */
    Long createSafetyEdu(RequestDto requestDto);
    
    /**
     * 기존 안전 교육 정보를 수정합니다.
     * 
     * @param id         수정할 안전 교육의 고유 ID
     * @param requestDto 수정할 정보가 담긴 요청 객체
     * @return 수정된 안전 교육의 고유 ID
     */
    Long updateSafetyEdu(Long id, RequestDto requestDto);
    /**
     * 지정된 ID의 안전 교육 정보를 삭제합니다.
     * 
     * @param id 삭제할 안전 교육의 고유 ID
     */
    void deleteSafetyEdu(Long id);
    
    /**
     * 다수의 안전 교육 정보를 일괄 삭제합니다.
     * 
     * @param ids 삭제할 안전 교육 ID 목록
     */
    void deleteSafetyEdus(List<Long> ids);
    
    /**
     * 다수의 안전 교육 정보에 대한 공개 여부 상태를 변경합니다.
     * 
     * @param ids      상태를 변경할 안전 교육 ID 목록
     * @param isPublic 변경할 공개 여부 값 (true: 공개, false: 비공개)
     */
    void updateVisibility(List<Long> ids, Boolean isPublic);
    
}
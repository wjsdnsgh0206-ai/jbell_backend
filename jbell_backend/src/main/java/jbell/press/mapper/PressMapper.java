package jbell.press.mapper;

import jbell.press.dto.PressDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface PressMapper {

    // 1. 관리자용: 보도자료 글 등록 (DB content 테이블)
    int insertContent(PressDTO pressDto);

    // 2. 관리자용: 첨부파일 정보 등록 (DB attachment 테이블)
    int insertAttachment(Map<String, Object> fileInfo);

    // 3. 사용자/관리자 공용: 목록 조회 (페이징 처리를 위해 limit, offset 사용)
    List<PressDTO> getPressList(@Param("offset") int offset, @Param("limit") int limit);
    
    
    // 4. 사용자/관리자 공용: 상세 조회
    PressDTO getPressById(@Param("contentId") Long contentId);

    // 5. 관리자용: 선택 삭제 (다건 삭제를 위해 List로 받음)
    int deletePress(@Param("ids") List<Long> ids);
    
    List<Map<String, Object>> getFileList(Long contentId);
    
    int updateContent(PressDTO pressDto);
	// 특정 ID들만 제외하고 삭제하는 기능
	int deleteAttachmentsExcludeIds(@Param("contentId") Long contentId, @Param("existingIds") List<Long> existingIds);
	
}
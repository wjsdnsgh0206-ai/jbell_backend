package jbell.common.mapper;

import jbell.common.domain.AttachmentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AttachmentMapper {
    /**
     * 파일 정보 저장 (insert 후 fileId 반환)
     */
    int insertAttachment(AttachmentVO attachment);

    /**
     * 게시글 저장 후, 임시로 올라간 파일들에 content_id 매핑
     */
    int updateContentId(
        @Param("contentId") Long contentId, 
        @Param("fileIds") List<Long> fileIds
    );
    
    /**
     * (선택) 게시글에 속한 파일 목록 조회
     */
    List<AttachmentVO> selectListByContentId(Long contentId);
}
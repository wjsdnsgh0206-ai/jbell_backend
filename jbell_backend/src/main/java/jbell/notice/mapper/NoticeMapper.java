package jbell.notice.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jbell.notice.dto.NoticeDTO;
import jbell.notice.entity.Notice;

@Mapper
public interface NoticeMapper {
	
	List<Notice> selectNoticeList();

	// 목록
	List<NoticeDTO> selectNoticeDTOList(@Param("keyword") String keyword, @Param("contentType") String contentType);

    // 상세
    NoticeDTO selectNoticeDTOById(Long id);

    void createNotice(Notice notice); // 등록
    void updateNotice(Notice notice); // 수정
    void deleteNotice(Long id);       // 삭제
    void increaseViews(Long id);	  // 조회수
    
    // 공지사항 타입 목록 조회 (추가된 메서드)
    List<Map<String, Object>> selectNoticeTypes();

    // 관리자 공지사항 조회 페이지에서는 사용, 미사용 게시물을 모두 조회할 수 있도록 관리자 전용으로 새로 추가
    List<NoticeDTO> selectAdminNoticeDTOList(
    	    @Param("keyword") String keyword,
    	    @Param("contentType") String contentType
    );

}


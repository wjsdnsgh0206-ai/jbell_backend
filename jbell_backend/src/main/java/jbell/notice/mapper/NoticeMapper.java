package jbell.notice.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jbell.notice.dto.NoticeDTO;
import jbell.notice.entity.Notice;

@Mapper
public interface NoticeMapper {
	
	List<Notice> selectNoticeList();

	// 목록
    List<NoticeDTO> selectNoticeDTOList();

    // 상세
    NoticeDTO selectNoticeDTOById(Long id);

    void createNotice(Notice notice); // 등록
    void updateNotice(Notice notice); // 수정
    void deleteNotice(Long id);       // 삭제
}


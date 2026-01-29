package jbell.notice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jbell.notice.mapper.NoticeMapper;
import jbell.notice.dto.NoticeDTO;
import jbell.notice.entity.Notice;

@Service
public class NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    public NoticeService(NoticeMapper noticeMapper) {
        this.noticeMapper = noticeMapper;
    }

    // 목록 (DTO)
    public List<NoticeDTO> getNoticeDTOList() {
        return noticeMapper.selectNoticeDTOList();
    }

    // 상세 (DTO)
    public NoticeDTO getNoticeDetail(Long id) {
        return noticeMapper.selectNoticeDTOById(id);
    }
    
    public void createNotice(Notice notice) {
        noticeMapper.createNotice(notice);
    }

    public void updateNotice(Notice notice) {
        noticeMapper.updateNotice(notice);
    }

    public void deleteNotice(Long id) {
        noticeMapper.deleteNotice(id);
    }
    
}

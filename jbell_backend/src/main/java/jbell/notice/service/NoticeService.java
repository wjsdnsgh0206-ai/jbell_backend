package jbell.notice.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jbell.notice.dto.NoticeDTO;
import jbell.notice.dto.NoticeFileDTO;
import jbell.notice.entity.Notice;
import jbell.notice.entity.NoticeFile;
import jbell.notice.mapper.NoticeFileMapper;
import jbell.notice.mapper.NoticeMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeMapper noticeMapper;
    // private final NoticeFileMapper noticeFileMapper;
    private final NoticeFileService noticeFileService;

    public List<Map<String, Object>> getNoticeTypes() {
        return noticeMapper.selectNoticeTypes();
    }

    // 목록 조회 시 contentType 필터링이 가능하도록 수정
    public List<NoticeDTO> getNoticeDTOList(String keyword, String contentType) {
        return noticeMapper.selectNoticeDTOList(keyword, contentType);
    }

    
    
    @Transactional
    public NoticeDTO getNoticeDetailAndIncreaseViews(Long id,
            HttpServletRequest request, HttpServletResponse response) {

        NoticeDTO notice = noticeMapper.selectNoticeDTOById(id);
        if (notice == null) {
            return null;
        }

        List<NoticeFileDTO> files = noticeFileService.getFilesByNoticeId(id);
        if (files == null) {
            files = new ArrayList<>();
        }

        notice.setFiles(convertToFileDTO(files));
        notice.setFileCount(files.size());

        return notice;
    }


    
    // throws IOException을 유지하거나, Controller에서 처리해야 합니다.
    @Transactional
    public void createNotice(Notice notice, List<MultipartFile> files) throws IOException {
        noticeMapper.createNotice(notice); 
        if (files != null && !files.isEmpty()) {
            noticeFileService.saveFiles(files, notice.getNoticeId());
        }
    }

    @Transactional
    public void updateNotice(Notice notice, List<MultipartFile> files, List<Long> deleteFileIds) throws IOException {
        noticeMapper.updateNotice(notice);
        if (deleteFileIds != null) {
            for (Long fileId : deleteFileIds) {
                noticeFileService.deleteFile(fileId);
            }
        }
        if (files != null && !files.isEmpty()) {
            noticeFileService.saveFiles(files, notice.getNoticeId());
        }
    }

    private List<NoticeFileDTO> convertToFileDTO(List<NoticeFileDTO> files) {
        return files.stream().map(f -> {
            NoticeFileDTO dto = new NoticeFileDTO();
            dto.setFileId(f.getFileId());
            dto.setFileName(f.getFileName());
            dto.setFileRealName(f.getFileRealName());
            dto.setFilePath(f.getFilePath());
            dto.setFileSize(f.getFileSize());
            dto.setFileExt(f.getFileExt());
            dto.setContentId(f.getContentId());
            return dto;
        }).collect(Collectors.toList());
    }

    public void deleteNotice(Long id) {
        noticeMapper.deleteNotice(id);
    }
}
package jbell.press.controller;

import jbell.press.dto.PressDTO;
import jbell.press.service.PressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/press")
public class AdminPressController {

    private final PressService pressService;

    /**
     * 보도자료 등록 (글 정보 + 파일들)
     */
    @PostMapping
    public ResponseEntity<Long> createPress(
            @RequestPart("data") PressDTO pressDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) throws Exception {
        
        Long contentId = pressService.savePress(pressDto, files);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(contentId);
    }
    
    /**
     * 보도자료 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePress(
    		@PathVariable("id") Long id,
            @RequestPart("data") PressDTO pressDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) throws Exception {
        
        pressDto.setContentId(id);
        pressService.updatePress(pressDto, files);
        
        return ResponseEntity.ok().build();
    }

    /**
     * 보도자료 선택 삭제
     */
    @DeleteMapping
    public ResponseEntity<Void> deletePress(@RequestBody List<Long> ids) {
        pressService.deletePress(ids);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 보도자료 일괄 노출, 비노출
     */
    @PatchMapping("/visible-status")
    public ResponseEntity<Void> updateVisibleStatus(@RequestBody Map<String, Object> params) {
        List<Integer> intIds = (List<Integer>) params.get("ids");
        List<Long> ids = intIds.stream()
                               .map(Integer::longValue)
                               .collect(Collectors.toList());
        String visibleYn = (String) params.get("visibleYn");

        pressService.updateVisibleStatus(ids, visibleYn);
        return ResponseEntity.ok().build();
    }
}

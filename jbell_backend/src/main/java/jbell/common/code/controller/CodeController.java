package jbell.common.code.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import jbell.common.code.dto.CodeGroupDTO;
import jbell.common.code.dto.CodeItemDTO;
import jbell.common.code.service.CodeService;

@RestController
@RequestMapping("/api/common/code")
@RequiredArgsConstructor
public class CodeController {

    private final CodeService codeService;

    @GetMapping("/groups")
    public List<CodeGroupDTO> getCodeGroups() {
        return codeService.getCodeGroupList(); // 여기서 DTO 리스트 반환
    }

    @PostMapping("/groups")
    public void addGroup(@RequestBody CodeGroupDTO dto) {
        codeService.registerGroup(dto);
    }

    @PutMapping("/groups/{id}") // 경로에 {id} 추가
    public void updateGroup(@PathVariable("id") String id, @RequestBody CodeGroupDTO dto) {
        // 경로로 받은 id를 DTO에 강제로 세팅해줌으로써 데이터 정합성을 맞춥니다.
        dto.setGroupCode(id); 
        codeService.modifyGroup(dto);
    }

 // 1. 삭제 매핑 수정 (groups 추가 및 ID 명시)
    @DeleteMapping("/groups/{id}")
    public void deleteGroup(@PathVariable("id") String id) { 
        codeService.removeGroup(id);
    }

    @GetMapping("/groups/{codeGroupId}/items")
    public List<CodeItemDTO> getCodeItems(@PathVariable("codeGroupId") String codeGroupId) {
        return codeService.getCodeItemList(codeGroupId);
    }

}
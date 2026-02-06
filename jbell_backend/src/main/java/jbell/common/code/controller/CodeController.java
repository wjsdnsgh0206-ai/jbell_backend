package jbell.common.code.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import jbell.common.code.dto.CodeGroupDTO;
import jbell.common.code.dto.CodeItemDTO;
import jbell.common.code.service.CodeService;

@RestController
@RequestMapping("/api/common/code")
@RequiredArgsConstructor
public class CodeController {

    private final CodeService codeService;
    
    // 공통코드 통합 조회
    @GetMapping("/all")
    public List<CodeItemDTO> getAllCodes() {
        return codeService.getIntegratedList();
    }
    
    // 그룹 중복 체크
    @GetMapping("/check/group")
    public ResponseEntity<Map<String, Boolean>> checkGroupDup(
            @RequestParam("groupCode") String groupCode,
            @RequestParam("groupName") String groupName) {
        
        Map<String, Boolean> result = new HashMap<>();
        result.put("isIdDup", codeService.isGroupCodeDuplicate(groupCode));
        result.put("isNameDup", codeService.isGroupNameDuplicate(groupName));
        return ResponseEntity.ok(result);
    }

    // 상세 중복 체크
    @GetMapping("/check/sub")
    public ResponseEntity<Map<String, Boolean>> checkSubDup(
            @RequestParam("groupCode") String groupCode,
            @RequestParam(value = "subCode", required = false) String subCode,
            @RequestParam(value = "subName", required = false) String subName) {
        
        Map<String, Boolean> result = new HashMap<>();
        
        // subCode가 넘어왔을 때만 중복 체크, 없으면 false
        boolean isCodeDup = (subCode != null && !subCode.trim().isEmpty()) 
                            ? codeService.isSubCodeDuplicate(groupCode, subCode) 
                            : false;
                            
        // subName이 넘어왔을 때만 중복 체크, 없으면 false
        boolean isNameDup = (subName != null && !subName.trim().isEmpty()) 
                            ? codeService.isSubNameDuplicate(groupCode, subName) 
                            : false;

        result.put("isCodeDup", isCodeDup);
        result.put("isNameDup", isNameDup);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/groups")
    public List<CodeGroupDTO> getCodeGroups() {
        return codeService.getCodeGroupList(); // 여기서 DTO 리스트 반환
    }
    
    // 특정 그룹 하나만 조회 (상세 페이지용)
    @GetMapping("/groups/{id}")
    public CodeGroupDTO getCodeGroup(@PathVariable("id") String id) {
        return codeService.getCodeGroup(id); // 단일 그룹 정보를 가져오는 서비스 호출
    }
    
    // 그룹 등록
    @PostMapping("/groups")
    public void addGroup(@Valid @RequestBody CodeGroupDTO dto) { // @Valid 추가
        codeService.registerGroup(dto);
    }
    
    // 그룹 수정
    @PutMapping("/groups/{id}")
    public void updateGroup(@PathVariable("id") String id, @Valid @RequestBody CodeGroupDTO dto) { // @Valid 추가
        dto.setGroupCode(id); 
        codeService.modifyGroup(dto);
    }

    // 그룹 삭제 (groups 추가 및 ID 명시)
    @DeleteMapping("/groups/{id}")
    public void deleteGroup(@PathVariable("id") String id) { 
        codeService.removeGroup(id);
    }
    
    
    @GetMapping("/groups/{codeGroupId}/items")
    public List<CodeItemDTO> getCodeItems(@PathVariable("codeGroupId") String codeGroupId) {
        return codeService.getCodeItemList(codeGroupId);
    }
    
    // 특정 상세 코드(아이템) 하나만 조회
    @GetMapping("/groups/{groupId}/items/{itemId}")
    public CodeItemDTO getCodeItem(
            @PathVariable("groupId") String groupId, 
            @PathVariable("itemId") String itemId) {
        
        return codeService.getCodeItem(groupId, itemId);
    }
    
    // 상세 코드 등록
    @PostMapping("/groups/{groupId}/items")
    public void addItem(@PathVariable("groupId") String groupId, @Valid @RequestBody CodeItemDTO dto) {
        dto.setGroupCode(groupId);
        codeService.registerItem(dto);
    }

    // 상세 코드 수정
    @PutMapping("/groups/{groupId}/items/{itemId}")
    public void updateItem(
        @PathVariable("groupId") String groupId,
        @PathVariable("itemId") String itemId,
        @Valid @RequestBody CodeItemDTO dto // @Valid 추가
    ) {
        dto.setGroupCode(groupId);
        dto.setSubCode(itemId);
        codeService.modifyItem(dto);
    }

    // 상세 코드 삭제
    @DeleteMapping("/groups/{groupId}/items/{itemId}")
    public void deleteItem(
        @PathVariable("groupId") String groupId,
        @PathVariable("itemId") String itemId
    ) {
        codeService.removeItem(groupId, itemId);
    }

}
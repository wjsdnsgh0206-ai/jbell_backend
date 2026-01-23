package jbell.common.code.service.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import jbell.common.code.domain.CodeGroup;
import jbell.common.code.dto.CodeGroupDTO;
import jbell.common.code.dto.CodeItemDTO;
import jbell.common.code.mapper.CodeMapper;
import jbell.common.code.service.CodeService;

@Service
@RequiredArgsConstructor
public class CodeServiceImpl implements CodeService {
    
    private final CodeMapper codeMapper;

    @Override
    public List<CodeGroupDTO> getCodeGroupList() {
        return codeMapper.selectCodeGroupList().stream()
                .map(g -> CodeGroupDTO.builder()
                        .groupCode(g.getCodeGroupId())
                        .groupName(g.getCodeGroupName())
                        .desc(g.getCodeDesc())
                        .order(g.getSortOrder())
                        .visible("Y".equals(g.getVisibleYn()))
                        .date(g.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void registerGroup(CodeGroupDTO dto) {
        CodeGroup group = CodeGroup.builder()
                .codeGroupId(dto.getGroupCode())
                .codeGroupName(dto.getGroupName())
                .codeDesc(dto.getDesc())
                .sortOrder(dto.getOrder())
                .visibleYn(dto.isVisible() ? "Y" : "N") // 리액트 boolean -> DB Y/N
                .build();
        codeMapper.insertCodeGroup(group);
    }

    @Override
    public void modifyGroup(CodeGroupDTO dto) {
        CodeGroup group = CodeGroup.builder()
                .codeGroupId(dto.getGroupCode()) // 컨트롤러에서 세팅한 ID가 여기 들어감
                .codeGroupName(dto.getGroupName())
                .codeDesc(dto.getDesc())
                .sortOrder(dto.getOrder())
                .visibleYn(dto.isVisible() ? "Y" : "N")
                .build();

        codeMapper.updateCodeGroup(group);
    }

    @Override
    public void removeGroup(String id) {
        codeMapper.deleteCodeGroup(id);
    }

    @Override
    public List<CodeItemDTO> getCodeItemList(String groupId) {
        // 상세 코드 조회 및 DTO 변환 로직 (위와 동일한 패턴)
        return codeMapper.selectCodeItemListByGroupId(groupId).stream()
                .map(i -> CodeItemDTO.builder()
                        .subCode(i.getCodeItemId())
                        .subName(i.getCodeItemName())
                        .visible("Y".equals(i.getVisibleYn()))
                        .build())
                .collect(Collectors.toList());
    }
}
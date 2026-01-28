package jbell.behaviorMethod.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jbell.behaviorMethod.domain.BehaviorMethodContentVO;
import jbell.behaviorMethod.dto.BehaviorMethod;

@Mapper
public interface BehaviorMethodMapper {
    /**
     * 행동요령 리스트를 content 테이블에 일괄 저장
     * @param list 행동요령 DTO 리스트
     * @param contentType 코드 아이템 ID (예: BEHAVIOR_METHOD_NATURAL)
     */
    int insertBehaviorMethodContents(
            @Param("list") List<BehaviorMethod> list, 
            @Param("contentType") String contentType
    );
    
	// description에 담긴 '01001' 같은 코드로 'NATURAL_TYPHOON'을 찾아옵니다.
    String findCodeItemIdByDescription(@Param("description") String description);
    
    // contentType에 맞는 행동요령 리스트 조회
    List<BehaviorMethodContentVO> selectBehaviorMethodList(String contentType);
}

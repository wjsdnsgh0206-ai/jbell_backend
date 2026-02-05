package jbell.behaviorMethod.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import jbell.behaviorMethod.domain.BehaviorMethodContentVO;
import jbell.behaviorMethod.dto.BehaviorMethod;

@Mapper
public interface BehaviorMethodMapper {
    
    // 1. [동기화 전처리] 기존 API 데이터의 최신 여부를 'N'으로 변경
    // (reg_type이 'API'인 것만 건드려서 관리자가 직접 등록한 데이터는 보호)
    void updateOldSyncDataToN(@Param("contentType") String contentType);

    // 2. [데이터 삽입] API 데이터 일괄 저장 (last_sync_yn='Y', reg_type='API')
    int insertBehaviorMethodContents(
            @Param("list") List<BehaviorMethod> list, 
            @Param("contentType") String contentType
    );
    
    // 3. [데이터 청소] 사용하지 않는(N) 과거 API 데이터 일괄 삭제
    void deleteOldSyncData();

    // 4. [유틸] 설명으로 코드 ID 찾기
    String findCodeItemIdByDescription(@Param("description") String description);
    
    // 5. [목록 조회] 
    // onlyLatest가 'Y'이면 -> (last_sync_yn='Y' OR reg_type='MANUAL') 데이터만 조회
    List<BehaviorMethodContentVO> selectBehaviorMethodList(
        @Param("contentType") String contentType, 
        @Param("visibleYn") String visibleYn,
        @Param("onlyLatest") String onlyLatest 
    );
    
    // 상세 조회
    BehaviorMethodContentVO selectBehaviorMethodDetail(@Param("contentId") Long contentId);

    // 단건 수정
    int updateBehaviorMethod(BehaviorMethodContentVO updateData);
    
    // 단건 등록 (관리자용)
    int insertManualBehaviorMethod(BehaviorMethodContentVO vo);
    
    /**
     * 행동요령 일괄 삭제
     * @param ids 삭제할 ID 리스트
     * @return 삭제된 행의 수
     */
    int deleteBehaviorMethods(@Param("ids") List<Long> ids);
    
    /**
     * 노출 상태 일괄 업데이트
     * @param ids ID 리스트
     * @param visibleYn 상태값
     */
    int updateVisibility(@Param("ids") List<Long> ids, @Param("visibleYn") String visibleYn);
}
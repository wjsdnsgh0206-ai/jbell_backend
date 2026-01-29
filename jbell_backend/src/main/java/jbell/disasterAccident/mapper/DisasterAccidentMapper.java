package jbell.disasterAccident.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import jbell.disaster.dto.PredictionInfoResponse;
import jbell.disaster.dto.PredictionInfoResponse;

// ====== DB연결 인터페이스 ======
// DTO는 api로 불러온 데이터를 담는 곳이고, Mapper는 dto를 가지고 DB로 가는 연결통로. 
@Mapper
public interface DisasterAccidentMapper {
	
	// ------ 재난문자이력 데이터 조회 ------
	// DB에 저장된 재난문자들을 다 가져오는 메서드. 
	// 데이터가 여러개니까 List 사용. 
	List<PredictionInfoResponse> selectDisasterMessageList();
	
	// 재난문자이력 api에서 가져온 데이터를 DB에 넣기 위한 메서드
	// DTO를 DB테이블에 저장하는 메서드. (DisasterMessageResponse는 DTO의 이름)
	int insertDisasterMessage(PredictionInfoResponse disasterMessageResponse); 
	
	
	
    long selectMaxSn(); 
    int insertDisasterMessageList(List<PredictionInfoResponse> list); // 한 번에 저장하는 형태

}

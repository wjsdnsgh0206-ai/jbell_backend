package jbell.disaster.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import jbell.disaster.dto.PredictionInfoResponse;

@Mapper
public interface DisasterMessageMapper {
    int insertDisasterMessages(List<PredictionInfoResponse> messages);
    
    List<PredictionInfoResponse> selectDisasterMessageList();
}
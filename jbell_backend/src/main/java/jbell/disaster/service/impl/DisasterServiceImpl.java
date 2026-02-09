package jbell.disaster.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jbell.disaster.dto.MainDisasterResponse;
import jbell.disaster.dto.PredictionInfoResponse;
import jbell.disaster.mapper.DisasterMapper;
import jbell.disaster.service.DisasterService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DisasterServiceImpl implements DisasterService {
	
	
	private final DisasterMapper disasterMapper;

    // 리스트 조회
	@Override
    @Transactional(readOnly = true)
    public List<PredictionInfoResponse> getSavedDisasterMessages(PredictionInfoResponse searchParams) {
        return disasterMapper.selectDisasterList(searchParams);
    }
	
	@Override
	@Transactional(readOnly = true)
	public List<PredictionInfoResponse> getDisasterMessages(PredictionInfoResponse searchParams) {
	    return disasterMapper.selectDisasterList(searchParams);
	}
	

    // [추가] 전체 개수 조회 구현
    @Override
    @Transactional(readOnly = true)
    public int getTotalCount(PredictionInfoResponse searchParams) {
        return disasterMapper.selectDisasterTotalCount(searchParams);
    }

    // 상세 조회 
    public PredictionInfoResponse getDisasterDetail(Long id) {
        return disasterMapper.selectDisasterDetail(id); 
    }


    @Override
    public boolean updateDisasterVisibility(String visibleYn, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("변경할 대상이 없습니다.");
        }

        int updatedCount =
            disasterMapper.updateDisasterVisibility(visibleYn, ids);

        return updatedCount > 0;
    }
    

    
    // 일괄 삭제 
    public void deleteDisasters(List<Long> ids) {
        disasterMapper.deleteDisasterLogical(ids);
    }
    
    @Transactional
    public void saveDisaster(PredictionInfoResponse dto) {
        disasterMapper.insertDisaster(dto);
    }

    @Transactional
    public void modifyDisaster(PredictionInfoResponse dto) {
        disasterMapper.updateDisaster(dto);
    }
    
    
    // ===============================  기상 특보 ========================
    
    // 기상특보 리스트 (검색 조건 적용)
    @Override
    @Transactional(readOnly = true)
    public List<PredictionInfoResponse> getSavedWeatherWarnings(PredictionInfoResponse searchParams) {
        return disasterMapper.selectWeatherList(searchParams);
    }

    // 기상특보 총 개수 (추가)
    @Override
    @Transactional(readOnly = true)
    public int getWeatherTotalCount(PredictionInfoResponse searchParams) {
        return disasterMapper.selectWeatherTotalCount(searchParams);
    }
    
    // 기상특보 리스트
    public List<PredictionInfoResponse> getSavedWeatherWarnings() {
    	return disasterMapper.selectWeatherList();
    }

    public PredictionInfoResponse getWeatherDetail(String key) {
        PredictionInfoResponse result = disasterMapper.selectWeatherDetail(key);
        System.out.println("DB 조회 결과: " + result); // 여기서 null이 나오는지 확인
        return result;
    }

    @Transactional
    public void saveWeather(PredictionInfoResponse dto) {
        disasterMapper.insertWeather(dto);
    }

    @Transactional
    public void modifyWeather(PredictionInfoResponse dto) {
        disasterMapper.updateWeather(dto);
    }

    @Transactional
    public boolean updateWeatherVisibility(List<String> ids, String visibleYn) {
        disasterMapper.updateWeatherVisibility(ids, visibleYn);
        boolean result = true;
        return result;
    }

    @Transactional
    public void deleteWeatherWarnings(List<String> keys) {
        disasterMapper.deleteWeatherLogical(keys);
    }
    
    
    
	// 메인화면 재난사고속보 serviceImpl ===========
    @Override
    public List<MainDisasterResponse> getRecentDisasters() {
        return disasterMapper.getCombinedDisasterList();
    }
}

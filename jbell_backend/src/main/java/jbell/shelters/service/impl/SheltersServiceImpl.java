package jbell.shelters.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import jbell.shelters.dto.SheltersDTO;
import jbell.shelters.eunm.ApiType;
import jbell.shelters.mapper.SheltersMapper;
import jbell.shelters.service.SheltersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SheltersServiceImpl implements SheltersService {

    private final SheltersMapper sheltersMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Transactional
    public void syncAllShelters() {
        syncOneApi("https://www.safetydata.go.kr/V2/api/DSSP-IF-10945", "3GHRW69P7QEQBGMT", ApiType.SHELTER_IMSI);
        syncOneApi("https://www.safetydata.go.kr/V2/api/DSSP-IF-10942", "B46160KXPEO76640", ApiType.SHELTER_HEAT);
        syncOneApi("https://www.safetydata.go.kr/V2/api/DSSP-IF-10804", "Z5SKGB47241A8DAC", ApiType.SHELTER_COLD);
        syncOneApi("https://www.safetydata.go.kr/V2/api/DSSP-IF-10943", "0DV6Z4SNNN8O5403", ApiType.SHELTER_EARTHQUAKE);
        syncOneApi("https://www.safetydata.go.kr/V2/api/DSSP-IF-10417", "8J0C1EZD0148CI3L", ApiType.SHELTER_NUCLEAR);
        syncOneApi("https://www.safetydata.go.kr/V2/api/DSSP-IF-00195", "616SG7EF4F8BTN93", ApiType.SHELTER_CIVIL);
    }
    
    @Override
    public void syncOneApi(String apiUrl, String serviceKey, ApiType type) {
        int pageNo = 1;
        int numOfRows = 1000;
        boolean hasMore = true;

        while (hasMore) {
            try {
                String fullUrl = String.format("%s?serviceKey=%s&numOfRows=%d&pageNo=%d&returnType=json", 
                                                apiUrl, serviceKey, numOfRows, pageNo);
                
                Map<String, Object> response = restTemplate.getForObject(fullUrl, Map.class);
                List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("body");

                if (items == null || items.isEmpty()) {
                    hasMore = false;
                    continue;
                }

//					이거는 전국꺼 받는거
//                List<SheltersDTO> dtoList = items.stream().map(item -> {
//                    String fullAddr = safeString(item.get(type.addrField));
//                    // 10945번 처럼 주소 필드가 비어있을 경우 대체 필드 사용
//                    if(fullAddr.isEmpty()) fullAddr = safeString(item.get("DTL_ADRES")); 
//
//                    String[] addrParts = fullAddr.split(" ");
                List<SheltersDTO> dtoList = items.stream()
                        .map(item -> {
                            // 1. 주소 추출 (기본 필드 또는 대체 필드)
                            String fullAddr = safeString(item.get(type.addrField));
                            if(fullAddr.isEmpty()) fullAddr = safeString(item.get("DTL_ADRES")); 
                            
                            // 2. 주소의 첫 단어(시도 명칭) 추출
                            String[] addrParts = fullAddr.split(" ");
                            String ctpv = addrParts.length > 0 ? addrParts[0] : "";
                            
                            return new Object[]{item, fullAddr, ctpv}; 
                        })
                        // 3. 필터링: 시도 명칭이 '전북'으로 시작하거나 '전라북도'인 경우
                        .filter(objArray -> {
                            String ctpv = (String) objArray[2];
                            return ctpv.startsWith("전북") || ctpv.equals("전라북도");
                        })
                        // 4. DTO 변환
                        .map(objArray -> {
                            Map<String, Object> item = (Map<String, Object>) objArray[0];
                            String fullAddr = (String) objArray[1];
                            String ctpv = (String) objArray[2];
                            String[] addrParts = fullAddr.split(" ");
                            
                    // ------------------여기서부터는 전북만 이나 전국 다 받는거난 코드 동일--------------------------------------------------------
                    // 좌표 처리
                    Double lat, lon;
                    if (type.latField.equals("DMS")) {
                        // 도분초 방식 (10417, 00195)
                        lat = calculateDegree(item.get("LAT_PROVIN"), item.get("LAT_MIN"), item.get("LAT_SEC"));
                        lon = calculateDegree(item.get("LOT_PROVIN"), item.get("LOT_MIN"), item.get("LOT_SEC"));
                    } else {
                        // 일반 좌표 방식 (10942, 10804 등)
                        lat = safeDouble(item.get(type.latField));
                        lon = safeDouble(item.get(type.lonField));
                    }

                    return SheltersDTO.builder()
                        .fcltNm(safeString(item.get(type.nameField)))
                        .fcltSeCd(type.apiId)
                        .ctpvNm(addrParts.length > 0 ? addrParts[0] : "")
                        .sggNm(addrParts.length > 1 ? addrParts[1] : "")
                        .roadNmAddr(fullAddr)
                        .lat(lat)
                        .lot(lon)
                        .opnYn(safeString(item.get("OPN_YN")).equals("N") ? "N" : "Y")
                        .useYn("Y")
                        .build();
                }).collect(Collectors.toList());
                
                
                log.info("첫 번째 데이터 확인: {}", dtoList.get(0));

                sheltersMapper.upsertShelters(dtoList);
                log.info("[{}] Page {} 저장 완료", type.apiId, pageNo);
                pageNo++;

            } catch (Exception e) {
                log.error("API [{}] 에러: {}", type.apiId, e.getMessage());
                hasMore = false;
            }
        }
    }

    private String safeString(Object obj) { return obj == null ? "" : String.valueOf(obj).trim(); }
    
    private Double safeDouble(Object obj) {
        try { return obj == null ? 0.0 : Double.parseDouble(String.valueOf(obj)); }
        catch (Exception e) { return 0.0; }
    }

    private Double calculateDegree(Object prov, Object min, Object sec) {
        try {
            double p = Double.parseDouble(String.valueOf(prov));
            double m = Double.parseDouble(String.valueOf(min));
            double s = Double.parseDouble(String.valueOf(sec));
            return p + (m / 60.0) + (s / 3600.0);
        } catch (Exception e) { return 0.0; }
    }
}

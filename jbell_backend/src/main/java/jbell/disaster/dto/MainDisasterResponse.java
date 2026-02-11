package jbell.disaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MainDisasterResponse {
	private String category; // 기상경보, 재난문자, 지진, 산불
	private String title; // 표시 제목
	private String eventDate; // 발생 일시 (String)
	private String displayYn; // 노출 여부
}

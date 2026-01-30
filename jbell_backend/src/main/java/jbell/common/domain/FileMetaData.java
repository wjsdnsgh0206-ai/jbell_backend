package jbell.common.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileMetaData {

	private Long fileIdx;
	private String fileOriginalName;
	private String fileNewName;
	private String filePath;
	private Long fileSize;
	
}














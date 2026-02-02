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
	private String fileType;
	private Long fileSize;

	public AttachmentVO toAttachmentVO() {
		
		return AttachmentVO.builder()
						   .contentId(fileIdx)
						   .fileExt(fileType)
						   .fileName(fileNewName)
						   .fileRealName(fileOriginalName)
						   .fileSize(fileSize)
						   .filePath(filePath)
						   .build();
	}
}














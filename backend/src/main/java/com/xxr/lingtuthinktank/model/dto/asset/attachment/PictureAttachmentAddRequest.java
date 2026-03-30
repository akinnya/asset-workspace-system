package com.xxr.lingtuthinktank.model.dto.asset.attachment;

import lombok.Data;
import java.io.Serializable;

@Data
public class PictureAttachmentAddRequest implements Serializable {
    private Long pictureId;
    private String fileUrl;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private static final long serialVersionUID = 1L;
}

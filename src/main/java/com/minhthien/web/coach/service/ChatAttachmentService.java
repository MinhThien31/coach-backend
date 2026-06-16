package com.minhthien.web.coach.service;

import com.minhthien.web.coach.enums.ChatMessageType;
import org.springframework.web.multipart.MultipartFile;

public interface ChatAttachmentService {

    UploadedAttachment upload(MultipartFile file);

    record UploadedAttachment(
            ChatMessageType messageType,
            String url,
            String publicId,
            String fileName,
            String mimeType,
            Long sizeBytes
    ) {
    }
}

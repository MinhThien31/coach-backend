package com.minhthien.web.coach.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.minhthien.web.coach.enums.ChatMessageType;
import com.minhthien.web.coach.exception.BadRequestException;
import com.minhthien.web.coach.service.ChatAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatAttachmentServiceImpl implements ChatAttachmentService {

    private static final long IMAGE_OR_PDF_MAX_BYTES = 25L * 1024 * 1024;
    private static final long VIDEO_MAX_BYTES = 200L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );
    private static final Set<String> ALLOWED_VIDEOS = Set.of(
            "video/mp4",
            "video/webm",
            "video/quicktime"
    );

    private final Cloudinary cloudinary;

    @Override
    public UploadedAttachment upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }

        String mimeType = normalizeMimeType(file.getContentType());
        ChatMessageType messageType = resolveMessageType(mimeType);
        validateSize(file.getSize(), messageType);

        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "coachfinder/chat",
                            "resource_type", "auto"
                    )
            );

            Object secureUrl = uploadResult.get("secure_url");
            Object publicId = uploadResult.get("public_id");
            if (secureUrl == null) {
                throw new BadRequestException("Upload failed");
            }

            return new UploadedAttachment(
                    messageType,
                    secureUrl.toString(),
                    publicId != null ? publicId.toString() : null,
                    normalizeFileName(file.getOriginalFilename()),
                    mimeType,
                    file.getSize()
            );
        } catch (IOException e) {
            throw new BadRequestException("Upload failed: " + e.getMessage());
        }
    }

    private String normalizeMimeType(String mimeType) {
        return mimeType == null ? "" : mimeType.trim().toLowerCase();
    }

    private ChatMessageType resolveMessageType(String mimeType) {
        if (ALLOWED_IMAGES.contains(mimeType)) {
            return ChatMessageType.IMAGE;
        }
        if ("application/pdf".equals(mimeType)) {
            return ChatMessageType.PDF;
        }
        if (ALLOWED_VIDEOS.contains(mimeType)) {
            return ChatMessageType.VIDEO;
        }
        throw new BadRequestException("Only PDF, image, and video files are supported");
    }

    private void validateSize(long sizeBytes, ChatMessageType messageType) {
        long maxBytes = messageType == ChatMessageType.VIDEO ? VIDEO_MAX_BYTES : IMAGE_OR_PDF_MAX_BYTES;
        if (sizeBytes > maxBytes) {
            throw new BadRequestException(
                    messageType == ChatMessageType.VIDEO
                            ? "Video must not exceed 200MB"
                            : "Image and PDF files must not exceed 25MB"
            );
        }
    }

    private String normalizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "attachment";
        }
        String normalized = fileName.replace("\\", "/");
        int slashIndex = normalized.lastIndexOf('/');
        return slashIndex >= 0 ? normalized.substring(slashIndex + 1) : normalized;
    }
}

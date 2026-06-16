package com.minhthien.web.coach.service;

import com.cloudinary.Cloudinary;
import com.minhthien.web.coach.exception.BadRequestException;
import com.minhthien.web.coach.service.impl.ChatAttachmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class ChatAttachmentServiceImplTest {

    @Test
    void uploadRejectsUnsupportedMimeType() {
        ChatAttachmentServiceImpl service = new ChatAttachmentServiceImpl(mock(Cloudinary.class));
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.txt",
                "text/plain",
                "hello".getBytes()
        );

        assertThatThrownBy(() -> service.upload(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only PDF, image, and video files are supported");
    }

    @Test
    void uploadRejectsImageOverTwentyFiveMegabytes() {
        ChatAttachmentServiceImpl service = new ChatAttachmentServiceImpl(mock(Cloudinary.class));
        MultipartFile file = oversizedFile("large.png", "image/png", (25L * 1024 * 1024) + 1);

        assertThatThrownBy(() -> service.upload(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("25MB");
    }

    @Test
    void uploadRejectsVideoOverTwoHundredMegabytes() {
        ChatAttachmentServiceImpl service = new ChatAttachmentServiceImpl(mock(Cloudinary.class));
        MultipartFile file = oversizedFile("large.mp4", "video/mp4", (200L * 1024 * 1024) + 1);

        assertThatThrownBy(() -> service.upload(file))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("200MB");
    }

    private MultipartFile oversizedFile(String fileName, String contentType, long size) {
        return new MultipartFile() {
            @Override
            public String getName() {
                return "file";
            }

            @Override
            public String getOriginalFilename() {
                return fileName;
            }

            @Override
            public String getContentType() {
                return contentType;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public long getSize() {
                return size;
            }

            @Override
            public byte[] getBytes() {
                return new byte[0];
            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(new byte[0]);
            }

            @Override
            public void transferTo(java.io.File dest) {
            }
        };
    }
}

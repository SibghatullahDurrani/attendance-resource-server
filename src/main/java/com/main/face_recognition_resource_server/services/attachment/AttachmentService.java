package com.main.face_recognition_resource_server.services.attachment;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public interface AttachmentService {
    Long saveAttachment(MultipartFile file) throws IOException;

    File getFile(String filename) throws FileNotFoundException;
}

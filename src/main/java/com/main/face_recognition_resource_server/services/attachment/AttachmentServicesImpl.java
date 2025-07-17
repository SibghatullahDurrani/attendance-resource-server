package com.main.face_recognition_resource_server.services.attachment;

import com.main.face_recognition_resource_server.domains.Attachment;
import com.main.face_recognition_resource_server.repositories.AttachmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Random;


@Slf4j
@Service
public class AttachmentServicesImpl implements AttachmentServices {
    private final String ATTACHMENT_PATH = "attachments";
    private final AttachmentRepository attachmentRepository;

    public AttachmentServicesImpl(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    @Override
    public Long saveAttachment(MultipartFile file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File is null");
        }
        String fileName = file.getOriginalFilename();
        String filePath = ATTACHMENT_PATH + File.separator + fileName;
        File targetFile = new File(filePath);
        log.info(targetFile.getParent());
        if (!Objects.equals(targetFile.getParent(), ATTACHMENT_PATH)) {
            throw new SecurityException("Unsupported File name");
        }
        Random random = new Random();
        boolean validFileName = false;
        while (!validFileName) {
            if (!targetFile.exists()) {
                Files.copy(file.getInputStream(), targetFile.toPath());
                validFileName = true;
            } else {
                int randomNum = random.nextInt(10000);
                fileName = randomNum + file.getOriginalFilename();
                filePath = ATTACHMENT_PATH + File.separator + fileName;
                targetFile = new File(filePath);
            }
        }
        Attachment attachment = Attachment.builder()
                .fileName(fileName)
                .filePath(ATTACHMENT_PATH + File.separator + fileName).
                build();
        Attachment attachmentSaved = attachmentRepository.saveAndFlush(attachment);
        return attachmentSaved.getId();
    }

    @Override
    public File getFile(String filename) throws FileNotFoundException {
        if (filename == null) {
            throw new IllegalArgumentException("Filename is null");
        }
        File fileToDownload = new File(ATTACHMENT_PATH + File.separator + filename);
        if (!Objects.equals(fileToDownload.getParent(), ATTACHMENT_PATH)) {
            throw new SecurityException("Unsupported File name");
        }
        if (!fileToDownload.exists()) {
            throw new FileNotFoundException("file not found!");
        }
        return fileToDownload;
    }
}

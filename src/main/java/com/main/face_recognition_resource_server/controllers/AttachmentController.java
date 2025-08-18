package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.attachment.AttachmentNameDTO;
import com.main.face_recognition_resource_server.services.attachment.AttachmentService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("attachments")
public class AttachmentController {
    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AttachmentNameDTO> addAttachment(@RequestParam("file") MultipartFile file) throws IOException {
        Long attachmentId = attachmentService.saveAttachment(file);
        return new ResponseEntity<>(AttachmentNameDTO.builder().id(attachmentId).build(), HttpStatus.CREATED);
    }

    @GetMapping("")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Resource> downloadAttachment(@RequestParam("file") String fileName) throws IOException {
        File file = attachmentService.getFile(fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(Files.newInputStream(file.toPath())));
    }
}

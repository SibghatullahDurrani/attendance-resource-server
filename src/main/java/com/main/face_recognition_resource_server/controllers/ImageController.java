package com.main.face_recognition_resource_server.controllers;

import com.main.face_recognition_resource_server.DTOS.images.UserSourceImageDTO;
import com.main.face_recognition_resource_server.DTOS.images.UserSourceImageRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("images")
public class ImageController {

    @GetMapping("face-image/{imageName}")
    public ResponseEntity<Resource> faceImage(@PathVariable String imageName) throws MalformedURLException {
        Path path = Paths.get("FaceRecognition/" + imageName);

        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);

    }

    @GetMapping("source-face-image/{imageName}")
    public ResponseEntity<Resource> sourceImage(@PathVariable String imageName) throws MalformedURLException {
        Path path = Paths.get("SourceFaces/" + imageName);

        Resource resource = new UrlResource(path.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

    @PostMapping("source")
    public ResponseEntity<List<UserSourceImageDTO>> sourceImageOfUsers(@RequestBody UserSourceImageRequestDTO userSourceImageRequestDTO) throws IOException {
        List<UserSourceImageDTO> userSourceImages = new ArrayList<>();
        for (Long userId : userSourceImageRequestDTO.getUserIds()) {
            String sourceImageURI = "SourceFaces/%s%s".formatted(userId, ".jpg");
            Path sourceImagePath = Paths.get(sourceImageURI);
            if (Files.exists(sourceImagePath)) {
                byte[] sourceImage = Files.readAllBytes(sourceImagePath);
                userSourceImages.add(UserSourceImageDTO.builder()
                        .userId(userId)
                        .image(sourceImage)
                        .build());
            } else {
                userSourceImages.add(UserSourceImageDTO.builder()
                        .userId(userId)
                        .image(null)
                        .build());
            }
        }
        return ResponseEntity.ok(userSourceImages);
    }
}

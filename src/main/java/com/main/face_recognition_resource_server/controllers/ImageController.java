package com.main.face_recognition_resource_server.controllers;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("images")
public class ImageController {

  @GetMapping("face-image/{imageName}")
  public ResponseEntity<Resource> getFaceImage(@PathVariable String imageName) throws MalformedURLException {
    Path path = Paths.get("FaceRecognition/" + imageName);

    Resource resource = new UrlResource(path.toUri());

    return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(resource);
  }

  @GetMapping("source-face-image/{imageName}")
  public ResponseEntity<Resource> getSourceImage(@PathVariable String imageName) throws MalformedURLException {
    Path path = Paths.get("SourceFaces/" + imageName);

    Resource resource = new UrlResource(path.toUri());

    return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(resource);
  }


}

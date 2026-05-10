package com.movie.controller;

import com.movie.domain.Document;
import com.movie.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired private FileService fileService;

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("userId") Long userId, @RequestParam("file") MultipartFile file) {
        return fileService.upload(userId,file);
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> download(@PathVariable String fileName) {
        return fileService.download(fileName);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocument(@PathVariable Long id) {
        return ResponseEntity.ok(fileService.getDocument(id));
    }
}

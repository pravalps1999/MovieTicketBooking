package com.movie.service;

import com.movie.Repository.DocumentRepository;
import com.movie.Repository.UserRepository;
import com.movie.domain.Document;
import com.movie.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileService {

    private final String UPLOAD_DIR = "uploads/";

    @Autowired private DocumentRepository documentRepository;
    @Autowired private UserRepository userRepository;

    // Upload + Save DB
    public String upload(Long userId,MultipartFile file) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            Path path = Paths.get(UPLOAD_DIR, fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());

            // Save metadata
            Document doc = new Document();
            doc.setFileName(fileName);
            doc.setFileType(file.getContentType());
            doc.setSize(file.getSize());
            doc.setUser(user);
            documentRepository.save(doc);

            return fileName+" uploaded successfully.";
        } catch (Exception e) {
            throw new RuntimeException("Upload failed");
        }
    }

    // Download logic
    public ResponseEntity<Resource> download(String fileName) {

        Path path = Paths.get(UPLOAD_DIR, fileName);
        Resource resource = new FileSystemResource(path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    // Cacheable DB fetch
    @Cacheable(value = "documents", key = "#id")
    public Document getDocument(Long id){
        return documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
    }
}
package com.example.storespring.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Tạo thư mục nếu chưa tồn tại
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Tạo tên file ngẫu nhiên tránh trùng lặp
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        // Lưu file vào thư mục uploads
        Path targetPath = Paths.get(uploadDir).resolve(uniqueFilename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Trả về đường dẫn để lưu vào database
        return "/uploads/" + uniqueFilename;
    }

    public void deleteFile(String filePath) {
        if (filePath == null || !filePath.startsWith("/uploads/")) {
            return;
        }
        try {
            String filename = filePath.replace("/uploads/", "");
            Path targetPath = Paths.get(uploadDir).resolve(filename);
            Files.deleteIfExists(targetPath);
        } catch (IOException ignored) {
        }
    }
}

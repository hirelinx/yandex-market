package ru.yandex.practicum.market.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.market.exception.NoSuchFileException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FilesServiceImpl implements FilesService {
    public final String uploadsDir;

    public FilesServiceImpl(@Value("${app.uploadsDir}") String uploadsDir) {
        this.uploadsDir = uploadsDir;
    }

    @Override
    public String upload(MultipartFile file, String newFileName) {
        try {
            Path uploadDir = Paths.get(uploadsDir);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Сохраняем файл
            Path filePath = uploadDir.resolve(newFileName);
            file.transferTo(filePath);

            return file.getOriginalFilename();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Resource download(String filename) {
        try {
            Path filePath = Paths.get(uploadsDir).resolve(filename).normalize();
            if (Files.notExists(filePath)) {
                throw new ru.yandex.practicum.market.exception.NoSuchFileException(filename);
            }
            byte[] content = Files.readAllBytes(filePath);

            return new ByteArrayResource(content);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


}

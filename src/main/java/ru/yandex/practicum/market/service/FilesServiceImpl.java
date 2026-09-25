package ru.yandex.practicum.market.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.yandex.practicum.market.exception.NoSuchFileException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FilesServiceImpl implements FilesService {

    private final Path uploadsDir;

    public FilesServiceImpl(@Value("${app.uploadsDir}") String uploadsDir) {
        this.uploadsDir = Paths.get(uploadsDir);

        try {
            Files.createDirectories(this.uploadsDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create uploads directory", e);
        }
    }

    @Override
    public Mono<String> upload(FilePart file, String newFileName) {
        Path filePath = uploadsDir.resolve(newFileName).normalize();

        return file.transferTo(filePath)
                .then(Mono.fromRunnable(() -> {
                    try {
                        if (Files.notExists(filePath)) {
                            Files.createFile(filePath);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to store uploaded file", e);
                    }
                }))
                .thenReturn(newFileName);
    }

    @Override
    public Mono<Resource> download(String filename) {
        Path filePath = uploadsDir.resolve(filename).normalize();

        return Mono.fromCallable(() -> {
                    if (Files.notExists(filePath)) {
                        throw new NoSuchFileException(filename);
                    }

                    return (Resource) new FileSystemResource(filePath);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
}

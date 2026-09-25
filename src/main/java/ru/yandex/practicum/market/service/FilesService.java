package ru.yandex.practicum.market.service;

import org.springframework.core.io.Resource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

public interface FilesService {
    Mono<String> upload(FilePart file, String newFileName);

    Mono<Resource> download(String filename);
}

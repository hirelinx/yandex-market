package ru.yandex.practicum.market.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FilesService {
    String upload(MultipartFile file, String newFileName);

    Resource download(String filename);
}

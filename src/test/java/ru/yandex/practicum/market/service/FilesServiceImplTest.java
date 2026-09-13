package ru.yandex.practicum.market.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import ru.yandex.practicum.market.exception.NoSuchFileException;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FilesServiceImplTest {
    @TempDir
    Path tempDir;

    private FilesService filesService;

    @BeforeEach
    void setUp() {
        filesService = new FilesServiceImpl(tempDir.toString() + "/");
    }

    @Test
    void uploadAndDownload_roundTrip() throws IOException {
        var file = new MockMultipartFile("image", "photo.jpg", "image/jpeg", "content".getBytes());

        filesService.upload(file, "stored.jpg");
        var resource = filesService.download("stored.jpg");

        assertThat(resource.getContentAsByteArray()).isEqualTo("content".getBytes());
    }

    @Test
    void download_missingFile_throwsNoSuchFileException() {
        assertThatThrownBy(() -> filesService.download("missing.jpg"))
                .isInstanceOf(NoSuchFileException.class);
    }
}

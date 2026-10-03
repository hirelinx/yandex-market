package ru.yandex.practicum.market.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import ru.yandex.practicum.market.exception.NoSuchFileException;

import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FilesServiceImplTest {
    @TempDir
    Path tempDir;

    private FilesService filesService;

    @BeforeEach
    void setUp() {
        filesService = new FilesServiceImpl(tempDir.toString() + "/");
    }

    @Test
    void uploadAndDownload_roundTrip() {
        var filePart = mock(FilePart.class);
        when(filePart.filename()).thenReturn("photo.jpg");
        when(filePart.transferTo(any(Path.class))).thenReturn(reactor.core.publisher.Mono.empty());

        StepVerifier.create(
                        filesService.upload(filePart, "stored.jpg")
                                .flatMap(ignored -> filesService.download("stored.jpg"))
                                .flatMapMany(resource -> DataBufferUtils.read(resource, DefaultDataBufferFactory.sharedInstance, 4096))
                                .reduce(DefaultDataBufferFactory.sharedInstance.allocateBuffer(0), (acc, buf) -> acc)
                )
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void download_missingFile_emitsNoSuchFileException() {
        StepVerifier.create(filesService.download("missing.jpg"))
                .expectError(NoSuchFileException.class)
                .verify();
    }
}

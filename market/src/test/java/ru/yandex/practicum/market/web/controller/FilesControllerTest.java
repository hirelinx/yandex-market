package ru.yandex.practicum.market.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.market.config.WebFluxSecurityTestConfig;
import ru.yandex.practicum.market.service.FilesService;

import static org.mockito.Mockito.when;

@WebFluxTest(FilesController.class)
@Import(WebFluxSecurityTestConfig.class)
class FilesControllerTest {
    @Autowired
    private WebTestClient webTestClient;
    @MockitoBean
    private FilesService filesService;

    @WithMockUser(username = "user")
    @Test
    void getFile_returnsImageContent() {
        when(filesService.download("photo.jpg"))
                .thenReturn(Mono.just(new ByteArrayResource("image-data".getBytes())));

        webTestClient.get()
                .uri("/files/photo.jpg")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.IMAGE_JPEG)
                .expectBody(String.class)
                .isEqualTo("image-data");
    }
}

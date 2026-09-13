package ru.yandex.practicum.market.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.market.service.FilesService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FilesController.class)
class FilesControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private FilesService filesService;

    @Test
    void getFile_returnsImageContent() throws Exception {
        when(filesService.download("photo.jpg"))
                .thenReturn(new ByteArrayResource("image-data".getBytes()));

        mockMvc.perform(get("/files/photo.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes("image-data".getBytes()));
    }
}

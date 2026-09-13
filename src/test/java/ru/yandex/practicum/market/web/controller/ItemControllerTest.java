package ru.yandex.practicum.market.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.dto.ItemsAndPagingDto;
import ru.yandex.practicum.market.service.CartService;
import ru.yandex.practicum.market.service.ItemService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ItemService itemService;
    @MockitoBean
    private CartService cartService;

    @Test
    void getItems_returnsItemsView() throws Exception {
        when(itemService.search(any(), any()))
                .thenReturn(new ItemsAndPagingDto(List.of(), false, false));

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attributeExists("items", "search", "sort", "paging"));
    }

    @Test
    void postItems_plusAction_redirectsToItems() throws Exception {
        mockMvc.perform(post("/items")
                        .param("id", "1")
                        .param("action", "PLUS")
                        .param("search", "")
                        .param("sort", "NO")
                        .param("pageNumber", "1")
                        .param("pageSize", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items?search=&sort=NO&pageNumber=1&pageSize=5"));

        verify(cartService).addToCart(1L);
    }

    @Test
    void getNewItem_returnsFormView() throws Exception {
        mockMvc.perform(get("/items/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("item-new"))
                .andExpect(model().attributeExists("itemCreateRequest"));
    }

    @Test
    void postNewItem_validRequest_redirectsToItemPage() throws Exception {
        when(itemService.create(any(), any()))
                .thenReturn(new ItemDto(7L, "Сыр", "desc", "files/a.jpg", BigDecimal.TEN, 0));
        var image = new MockMultipartFile("image", "photo.jpg", "image/jpeg", "data".getBytes());

        mockMvc.perform(multipart("/items/new")
                        .file(image)
                        .param("title", "Сыр")
                        .param("description", "desc")
                        .param("price", "10.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items/7"));
    }

    @Test
    void postNewItem_withoutImage_returnsFormWithErrors() throws Exception {
        var emptyImage = new MockMultipartFile("image", "", "application/octet-stream", new byte[0]);

        mockMvc.perform(multipart("/items/new")
                        .file(emptyImage)
                        .param("title", "Сыр")
                        .param("description", "desc")
                        .param("price", "10.00"))
                .andExpect(status().isOk())
                .andExpect(view().name("item-new"));
    }

    @Test
    void getItem_returnsItemView() throws Exception {
        when(itemService.retrieveById(3L))
                .thenReturn(new ItemDto(3L, "Хлеб", "desc", "files/b.jpg", BigDecimal.ONE, 0));

        mockMvc.perform(get("/items/3"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attributeExists("item"));
    }

    @Test
    void postItem_minusAction_returnsItemView() throws Exception {
        when(itemService.retrieveById(3L))
                .thenReturn(new ItemDto(3L, "Хлеб", "desc", "files/b.jpg", BigDecimal.ONE, 1));

        mockMvc.perform(post("/items/3").param("action", "MINUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"));

        verify(cartService).removeFromCart(3L);
    }
}

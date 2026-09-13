package ru.yandex.practicum.market.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(CartController.class)
class CartControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ItemService itemService;
    @MockitoBean
    private CartService cartService;

    @Test
    void getCartItems_returnsCartView() throws Exception {
        when(itemService.search(any(), any()))
                .thenReturn(new ItemsAndPagingDto(List.of(
                        new ItemDto(1L, "Молоко", "desc", "files/a.jpg", BigDecimal.TEN, 1)
                ), false, false));

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("items", "total"));
    }

    @Test
    void postCartItems_plusAction_returnsCartView() throws Exception {
        when(itemService.search(any(), any()))
                .thenReturn(new ItemsAndPagingDto(List.of(), false, false));

        mockMvc.perform(post("/cart/items")
                        .param("id", "2")
                        .param("action", "PLUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"));

        verify(cartService).addToCart(2L);
    }
}

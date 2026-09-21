package ru.yandex.practicum.market.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.market.dto.OrderDto;
import ru.yandex.practicum.market.service.OrderService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RootController.class)
class RootControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private OrderService orderService;

    @Test
    void root_redirectsToItems() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("items"));
    }

    @Test
    void buy_createsOrderAndRedirects() throws Exception {
        when(orderService.createFromCart())
                .thenReturn(new OrderDto(4L, List.of(), BigDecimal.ZERO));

        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/4?newOrder=true"));
    }
}

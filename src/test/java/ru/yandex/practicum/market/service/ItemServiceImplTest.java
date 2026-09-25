package ru.yandex.practicum.market.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.dto.ItemsAndPagingDto;
import ru.yandex.practicum.market.exception.EntityNotFoundException;
import ru.yandex.practicum.market.mapper.ItemMapper;
import ru.yandex.practicum.market.model.Cart;
import ru.yandex.practicum.market.model.CountedItem;
import ru.yandex.practicum.market.model.Item;
import ru.yandex.practicum.market.repository.ItemRepository;
import ru.yandex.practicum.market.support.TestEntityFactory;
import ru.yandex.practicum.market.web.request.ItemCreateRequest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private FilesService filesService;
    @Mock
    private CartService cartService;
    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void search_returnsItemsWithCartCounts() {
        var item = TestEntityFactory.item("Молоко", BigDecimal.TEN);
        item.setId(1L);
        var countedItem = TestEntityFactory.countedItem(1L, 2L);
        countedItem.setId(10L);

        when(itemRepository.findAllByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("", "", PageRequest.of(0, 5)))
                .thenReturn(Flux.just(item));
        when(cartService.retrieveCartEntity()).thenReturn(Mono.just(TestEntityFactory.cart(1L)));
        when(cartService.getCountedItems(any(Cart.class))).thenReturn(Flux.just(countedItem));
        when(itemMapper.toDto(item, 2L)).thenReturn(new ItemDto(1L, "Молоко", "desc", "files/test.jpg", BigDecimal.TEN, 2));

        var expectedDto = new ItemDto(1L, "Молоко", "desc", "files/test.jpg", BigDecimal.TEN, 2);
        StepVerifier.create(itemService.search("", PageRequest.of(0, 5)))
                .assertNext(result -> assertEquals(new ItemsAndPagingDto(List.of(expectedDto), false, false), result))
                .verifyComplete();
    }

    @Test
    void retrieveById_existingItem_returnsDto() {
        var item = TestEntityFactory.item("Хлеб", BigDecimal.ONE);
        item.setId(5L);
        when(itemRepository.findById(5L)).thenReturn(Mono.just(item));
        when(cartService.retrieveCartEntity()).thenReturn(Mono.just(TestEntityFactory.cart(1L)));
        when(cartService.getCountedItems(any(Cart.class))).thenReturn(Flux.empty());
        when(itemMapper.toDto(item, 0L)).thenReturn(new ItemDto(5L, "Хлеб", "desc", "files/test.jpg", BigDecimal.ONE, 0));

        StepVerifier.create(itemService.retrieveById(5L))
                .expectNextMatches(dto -> dto.id() == 5L)
                .verifyComplete();
    }

    @Test
    void retrieveById_missingItem_emitsEntityNotFoundException() {
        when(itemRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(itemService.retrieveById(99L))
                .expectError(EntityNotFoundException.class)
                .verify();
    }

    @Test
    void create_uploadsImageAndSavesItem() {
        var request = new ItemCreateRequest("Сыр", "Описание", BigDecimal.valueOf(150));
        var image = mock(FilePart.class);
        when(image.filename()).thenReturn("cheese.jpg");
        when(filesService.upload(eq(image), any(String.class))).thenReturn(Mono.just("cheese.jpg"));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item saved = invocation.getArgument(0);
            saved.setId(10L);
            return Mono.just(saved);
        });
        when(itemMapper.toDto(any(Item.class), eq(0L)))
                .thenReturn(new ItemDto(10L, "Сыр", "Описание", "files/uuid.jpg", BigDecimal.valueOf(150), 0));

        StepVerifier.create(itemService.create(request, image))
                .expectNextMatches(dto -> "Сыр".equals(dto.title()))
                .verifyComplete();

        verify(filesService).upload(eq(image), any(String.class));
        verify(itemRepository).save(any(Item.class));
    }
}

package ru.yandex.practicum.market.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import ru.yandex.practicum.market.dto.ItemDto;
import ru.yandex.practicum.market.exception.EntityNotFoundException;
import ru.yandex.practicum.market.mapper.ItemMapper;
import ru.yandex.practicum.market.model.Item;
import ru.yandex.practicum.market.repository.ItemRepository;
import ru.yandex.practicum.market.support.TestEntityFactory;
import ru.yandex.practicum.market.web.request.ItemCreateRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

        var countedItem = TestEntityFactory.countedItem(item, 2L);
        var cart = TestEntityFactory.emptyCart();
        cart.getCountedItems().add(countedItem);

        var page = (Page<Item>) new PageImpl<>(List.of(item), PageRequest.of(0, 5), 1);
        when(itemRepository.findAllByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase("", "", PageRequest.of(0, 5)))
                .thenReturn(page);
        when(cartService.retrieveCartEntity())
                .thenReturn(cart);
        when(itemMapper.toDto(item, 2L)).thenReturn(new ItemDto(1L, "Молоко", "desc", "files/test.jpg", BigDecimal.TEN, 2));

        var result = itemService.search("", PageRequest.of(0, 5));

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().count()).isEqualTo(2);
    }

    @Test
    void retrieveById_existingItem_returnsDto() {
        var item = TestEntityFactory.item("Хлеб", BigDecimal.ONE);
        item.setId(5L);
        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));
        when(cartService.retrieveCartEntity()).thenReturn(TestEntityFactory.emptyCart());
        when(itemMapper.toDto(item, 0L)).thenReturn(new ItemDto(5L, "Хлеб", "desc", "files/test.jpg", BigDecimal.ONE, 0));

        var result = itemService.retrieveById(5L);

        assertThat(result.id()).isEqualTo(5L);
    }

    @Test
    void retrieveById_missingItem_throwsEntityNotFoundException() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.retrieveById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void create_uploadsImageAndSavesItem() {
        var request = new ItemCreateRequest("Сыр", "Описание", BigDecimal.valueOf(150));
        var image = new MockMultipartFile("image", "cheese.jpg", "image/jpeg", "img".getBytes());
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });
        when(itemMapper.toDto(any(Item.class), eq(0L)))
                .thenReturn(new ItemDto(10L, "Сыр", "Описание", "files/uuid.jpg", BigDecimal.valueOf(150), 0));

        var result = itemService.create(request, image);

        verify(filesService).upload(eq(image), any(String.class));
        verify(itemRepository).save(any(Item.class));
        assertThat(result.title()).isEqualTo("Сыр");
    }
}

package ru.yandex.practicum.market.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.market.exception.EntityNotFoundException;
import ru.yandex.practicum.market.mapper.ItemMapper;
import ru.yandex.practicum.market.model.CountedItem;
import ru.yandex.practicum.market.repository.CartRepository;
import ru.yandex.practicum.market.repository.CountedItemRepository;
import ru.yandex.practicum.market.repository.ItemRepository;
import ru.yandex.practicum.market.support.TestEntityFactory;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {
    @Mock
    private CartRepository cartRepository;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private CountedItemRepository countedItemRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void addToCart_newItem_returnsTrue() {
        var item = TestEntityFactory.item("Товар", BigDecimal.TEN);
        item.setId(1L);
        var cart = TestEntityFactory.emptyCart();
        when(itemRepository.findById(1L)).thenReturn(java.util.Optional.of(item));
        when(cartRepository.findAll()).thenReturn(List.of(cart));

        assertThat(cartService.addToCart(1L)).isTrue();
        verify(countedItemRepository).save(org.mockito.ArgumentMatchers.any(CountedItem.class));
        verify(cartRepository).save(cart);
    }

    @Test
    void addToCart_existingItemInCart_incrementsCount() {
        var item = TestEntityFactory.item("Товар", BigDecimal.TEN);
        item.setId(1L);
        var countedItem = TestEntityFactory.countedItem(item, 1L);
        var cart = TestEntityFactory.emptyCart();
        cart.getCountedItems().add(countedItem);
        when(cartRepository.findAll()).thenReturn(List.of(cart));

        assertThat(cartService.addToCart(1L)).isFalse();
        assertThat(countedItem.getCount()).isEqualTo(2L);
        verify(countedItemRepository).save(countedItem);
    }

    @Test
    void addToCart_itemOnlyInOrder_createsNewCartItem() {
        var item = TestEntityFactory.item("Товар", BigDecimal.TEN);
        item.setId(1L);
        var cart = TestEntityFactory.emptyCart();
        when(itemRepository.findById(1L)).thenReturn(java.util.Optional.of(item));
        when(cartRepository.findAll()).thenReturn(List.of(cart));

        assertThat(cartService.addToCart(1L)).isTrue();
        assertThat(cart.getCountedItems()).hasSize(1);
        assertThat(cart.getCountedItems().iterator().next().getCount()).isEqualTo(1L);
        verify(countedItemRepository).save(org.mockito.ArgumentMatchers.any(CountedItem.class));
    }

    @Test
    void addToCart_missingItem_throwsEntityNotFoundException() {
        var cart = TestEntityFactory.emptyCart();
        when(cartRepository.findAll()).thenReturn(List.of(cart));
        when(itemRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> cartService.addToCart(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void removeFromCart_decrementsCount() {
        var item = TestEntityFactory.item("Товар", BigDecimal.TEN);
        item.setId(1L);
        var countedItem = TestEntityFactory.countedItem(item, 2L);
        var cart = TestEntityFactory.emptyCart();
        cart.getCountedItems().add(countedItem);
        when(cartRepository.findAll()).thenReturn(List.of(cart));

        assertThat(cartService.removeFromCart(1L)).isTrue();
        assertThat(countedItem.getCount()).isEqualTo(1L);
    }

    @Test
    void removeFromCart_lastItem_deletesCountedItem() {
        var item = TestEntityFactory.item("Товар", BigDecimal.TEN);
        item.setId(1L);
        var countedItem = TestEntityFactory.countedItem(item, 1L);
        var cart = TestEntityFactory.emptyCart();
        cart.getCountedItems().add(countedItem);
        when(cartRepository.findAll()).thenReturn(List.of(cart));

        assertThat(cartService.removeFromCart(1L)).isFalse();
        verify(countedItemRepository).delete(countedItem);
    }

    @Test
    void removeFromCart_missingItem_returnsFalse() {
        when(cartRepository.findAll()).thenReturn(List.of(TestEntityFactory.emptyCart()));

        assertThat(cartService.removeFromCart(1L)).isFalse();
    }

    @Test
    void retrieveCartEntity_returnsFirstCart() {
        var cart = TestEntityFactory.emptyCart();
        when(cartRepository.findAll()).thenReturn(List.of(cart));

        assertThat(cartService.retrieveCartEntity()).isSameAs(cart);
    }
}

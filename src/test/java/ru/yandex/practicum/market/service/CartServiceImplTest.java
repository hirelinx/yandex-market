package ru.yandex.practicum.market.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.market.exception.EntityNotFoundException;
import ru.yandex.practicum.market.mapper.ItemMapper;
import ru.yandex.practicum.market.model.Cart;
import ru.yandex.practicum.market.model.CartCountedItems;
import ru.yandex.practicum.market.model.CountedItem;
import ru.yandex.practicum.market.repository.CartCountedItemsRepository;
import ru.yandex.practicum.market.repository.CartRepository;
import ru.yandex.practicum.market.repository.CountedItemRepository;
import ru.yandex.practicum.market.repository.ItemRepository;
import ru.yandex.practicum.market.support.TestEntityFactory;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {
    @Mock
    private CartRepository cartRepository;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private CartCountedItemsRepository cartCountedItemsRepository;
    @Mock
    private CountedItemRepository countedItemRepository;
    @Mock
    private ItemRepository itemRepository;
    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void addToCart_newItem_returnsTrue() {
        var cart = TestEntityFactory.cart(1L);
        var item = TestEntityFactory.item("Товар", BigDecimal.TEN);
        item.setId(1L);
        var savedCountedItem = TestEntityFactory.countedItem(1L, 1L);
        savedCountedItem.setId(100L);

        when(cartRepository.findFirstBy()).thenReturn(Mono.just(cart));
        when(cartCountedItemsRepository.findByCartId(1L)).thenReturn(Flux.empty());
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item));
        when(countedItemRepository.save(any(CountedItem.class))).thenReturn(Mono.just(savedCountedItem));
        when(cartCountedItemsRepository.save(any(CartCountedItems.class))).thenReturn(Mono.just(TestEntityFactory.cartLink(1L, 100L)));

        StepVerifier.create(cartService.addToCart(1L))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void addToCart_existingItemInCart_incrementsCount() {
        var cart = TestEntityFactory.cart(1L);
        var countedItem = TestEntityFactory.countedItem(1L, 1L);
        countedItem.setId(100L);

        when(cartRepository.findFirstBy()).thenReturn(Mono.just(cart));
        when(cartCountedItemsRepository.findByCartId(1L))
                .thenReturn(Flux.just(TestEntityFactory.cartLink(1L, 100L)));
        when(countedItemRepository.findById(100L)).thenReturn(Mono.just(countedItem));
        when(countedItemRepository.save(countedItem)).thenReturn(Mono.just(countedItem));

        StepVerifier.create(cartService.addToCart(1L))
                .expectNext(false)
                .verifyComplete();

        assertThat(countedItem.getCount()).isEqualTo(2L);
    }

    @Test
    void addToCart_missingItem_emitsEntityNotFoundException() {
        var cart = TestEntityFactory.cart(1L);
        when(cartRepository.findFirstBy()).thenReturn(Mono.just(cart));
        when(cartCountedItemsRepository.findByCartId(1L)).thenReturn(Flux.empty());
        when(itemRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.addToCart(1L))
                .expectError(EntityNotFoundException.class)
                .verify();
    }

    @Test
    void removeFromCart_decrementsCount() {
        var cart = TestEntityFactory.cart(1L);
        var countedItem = TestEntityFactory.countedItem(1L, 2L);
        countedItem.setId(100L);

        when(cartRepository.findFirstBy()).thenReturn(Mono.just(cart));
        when(cartCountedItemsRepository.findByCartId(1L)).thenReturn(Flux.just(TestEntityFactory.cartLink(1L, 100L)));
        when(countedItemRepository.findById(100L)).thenReturn(Mono.just(countedItem));
        when(countedItemRepository.save(countedItem)).thenReturn(Mono.just(countedItem));

        StepVerifier.create(cartService.removeFromCart(1L))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void removeFromCart_lastItem_deletesCountedItem() {
        var cart = TestEntityFactory.cart(1L);
        var countedItem = TestEntityFactory.countedItem(1L, 1L);
        countedItem.setId(100L);

        when(cartRepository.findFirstBy()).thenReturn(Mono.just(cart));
        when(cartCountedItemsRepository.findByCartId(1L)).thenReturn(Flux.just(TestEntityFactory.cartLink(1L, 100L)));
        when(countedItemRepository.findById(100L)).thenReturn(Mono.just(countedItem));
        when(cartCountedItemsRepository.deleteCartCountedItemsByCountedItemsId(100L)).thenReturn(Mono.empty());
        when(countedItemRepository.delete(countedItem)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.removeFromCart(1L))
                .expectNext(false)
                .verifyComplete();

        verify(countedItemRepository).delete(countedItem);
    }

    @Test
    void removeFromCart_missingItem_returnsFalse() {
        var cart = TestEntityFactory.cart(1L);
        when(cartRepository.findFirstBy()).thenReturn(Mono.just(cart));
        when(cartCountedItemsRepository.findByCartId(1L)).thenReturn(Flux.empty());

        StepVerifier.create(cartService.removeFromCart(1L))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void retrieveCartEntity_returnsFirstCart() {
        var cart = TestEntityFactory.cart(1L);
        when(cartRepository.findFirstBy()).thenReturn(Mono.just(cart));

        StepVerifier.create(cartService.retrieveCartEntity())
                .expectNext(cart)
                .verifyComplete();
    }
}

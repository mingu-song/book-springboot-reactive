package mingu.bookreactive.service;

import mingu.bookreactive.entity.Cart;
import mingu.bookreactive.entity.CartItem;
import mingu.bookreactive.entity.Item;
import mingu.bookreactive.repository.CartRepository;
import mingu.bookreactive.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class InventoryServiceTest {
    private InventoryService inventoryService;

    @MockBean
    private ItemRepository itemRepository;

    @MockBean
    private CartRepository cartRepository;

    @BeforeEach
    void setUp() {
        Item sampleItem = new Item("item1", "TV tray", "Alf TV tray", 19.99);
        CartItem sampleCartItem = new CartItem(sampleItem);
        Cart sampleCart = new Cart("My Cart", Collections.singletonList(sampleCartItem));

        when(cartRepository.findById(anyString())).thenReturn(Mono.empty());
        when(itemRepository.findById(anyString())).thenReturn(Mono.just(sampleItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(sampleCart));

        inventoryService = new InventoryService(itemRepository, cartRepository);
    }

    @Test
    void addItemToEmptyCartShouldProduceOneCartItem() {
        // 조금 더 검증하고자 하는 함수 중심적이라 볼 수 있음
        inventoryService.addItemToCart("My Cart", "item1")
                .as(StepVerifier::create) // 구독자
                .expectNextMatches(cart -> {
                    assertThat(cart.getCartItems()).extracting(CartItem::getQuantity)
                            .containsExactlyInAnyOrder(1);
                    assertThat(cart.getCartItems()).extracting(CartItem::getItem)
                            .containsExactly(new Item("item1", "TV tray", "Alf TV tray", 19.99));
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void alternativeWayToTest() {
        StepVerifier.create(inventoryService.addItemToCart("My Cart", "item1"))
                .expectNextMatches(cart -> {
                    assertThat(cart.getCartItems()).extracting(CartItem::getQuantity)
                            .containsExactlyInAnyOrder(1);
                    assertThat(cart.getCartItems()).extracting(CartItem::getItem)
                            .containsExactly(new Item("item1", "TV tray", "Alf TV tray", 19.99));
                    return true;
                })
                .verifyComplete();
    }

}
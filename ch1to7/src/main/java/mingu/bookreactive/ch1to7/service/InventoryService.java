package mingu.bookreactive.ch1to7.service;

import mingu.bookreactive.ch1to7.entity.Cart;
import mingu.bookreactive.ch1to7.entity.CartItem;
import mingu.bookreactive.ch1to7.entity.Item;
import mingu.bookreactive.ch1to7.repository.CartRepository;
import mingu.bookreactive.ch1to7.repository.ItemRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
public class InventoryService {
    private final ItemRepository itemRepository;
    private final CartRepository cartRepository;

    public InventoryService(ItemRepository itemRepository, CartRepository cartRepository) {
        this.itemRepository = itemRepository;
        this.cartRepository = cartRepository;
    }

    public Mono<Cart> getCart(String cartId) {
        return this.cartRepository.findById(cartId);
    }

    public Flux<Item> getInventory() {
        return this.itemRepository.findAll();
    }

    public Mono<Item> saveItem(Item newItem) {
        return this.itemRepository.save(newItem);
    }

    public Mono<Void> deleteItem(String id) {
        return this.itemRepository.deleteById(id);
    }

    public Mono<Cart> addItemToCart(String cartId, String itemId) {
        return this.cartRepository.findById(cartId)
                .defaultIfEmpty(new Cart(cartId))
                .flatMap(cart -> cart.getCartItems().stream()
                        .filter(cartItem -> cartItem.getItem().getId().equals(itemId))
                        .findAny()
                        .map(cartItem -> {
                            cartItem.increment();
                            return Mono.just(cart);
                        })
                        .orElseGet(() -> {
                            return this.itemRepository.findById(itemId)
                                    .map(CartItem::new)
                                    .map(cartItem -> {
                                        cart.getCartItems().add(cartItem);
                                        return cart;
                                    });
                        }))
                .flatMap(this.cartRepository::save);
    }

    public Mono<Cart> removeOneFromCart(String cartId, String itemId) {
        return this.cartRepository.findById(cartId)
                .defaultIfEmpty(new Cart(cartId))
                .flatMap(cart -> cart.getCartItems().stream()
                        .filter(cartItem -> cartItem.getItem().getId().equals(itemId))
                        .findAny()
                        .map(cartItem -> {
                            cartItem.decrement();
                            return Mono.just(cart);
                        })
                        .orElse(Mono.empty()))
                .map(cart -> new Cart(cart.getId(), cart.getCartItems().stream()
                        .filter(cartItem -> cartItem.getQuantity() > 0)
                        .collect(Collectors.toList())))
                .flatMap(this.cartRepository::save);
    }

    public Flux<Item> searchByExample(String name, String description, boolean useAnd) {
        Item item = new Item(name, description, 0.0);

        ExampleMatcher matcher = (useAnd ? ExampleMatcher.matchingAll() : ExampleMatcher.matchingAny())
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase()
                .withIgnorePaths("price");

        Example<Item> probe = Example.of(item, matcher);
        return itemRepository.findAll(probe);
    }
}

package mingu.bookreactive.controller;

import mingu.bookreactive.entity.Cart;
import mingu.bookreactive.entity.Item;
import mingu.bookreactive.repository.CartRepository;
import mingu.bookreactive.repository.ItemRepository;
import mingu.bookreactive.service.CartService;
import mingu.bookreactive.service.InventoryService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
public class HomeController {

    private final ItemRepository itemRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final InventoryService inventoryService;

    public HomeController(ItemRepository itemRepository, CartRepository cartRepository, CartService cartService, InventoryService inventoryService) {
        this.itemRepository = itemRepository;
        this.cartRepository = cartRepository;
        this.cartService = cartService;
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public Mono<Rendering> home() {
        return Mono.just(Rendering.view("home.html")
                .modelAttribute("items", this.itemRepository.findAll())
                .modelAttribute("cart", this.cartRepository.findById("My Cart").defaultIfEmpty(new Cart("My Cart")))
                .build());
    }

    @PostMapping("/add/{id}")
    public Mono<String> addToCart(@PathVariable String id) {
        return this.cartService.addToCart("My Cart", id).thenReturn("redirect:/");
    }

    @PostMapping
    public Mono<String> createItem(@ModelAttribute Item newItem) {
        return this.itemRepository.save(newItem).thenReturn("redirect:/");
    }

    @DeleteMapping("/delete/{id}")
    public Mono<String> deleteItem(@PathVariable String id) {
        return this.itemRepository.deleteById(id).thenReturn("redirect:/");
    }

    @GetMapping("/search")
    public Mono<Rendering> search(@RequestParam(required = false) String name,
                                  @RequestParam(required = false) String description,
                                  @RequestParam boolean useAnd) {
        return Mono.just(Rendering.view("home.html")
                .modelAttribute("items", inventoryService.searchByExample(name, description, useAnd))
                .modelAttribute("cart", this.cartRepository.findById("My Cart").defaultIfEmpty(new Cart("My Cart")))
                .build());
    }
}

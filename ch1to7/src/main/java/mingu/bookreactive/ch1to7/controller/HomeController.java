package mingu.bookreactive.ch1to7.controller;

import mingu.bookreactive.ch1to7.entity.Cart;
import mingu.bookreactive.ch1to7.entity.Item;
import mingu.bookreactive.ch1to7.service.InventoryService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
public class HomeController {

    private final InventoryService inventoryService;

    public HomeController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public Mono<Rendering> home() {
        return Mono.just(Rendering.view("home.html")
                .modelAttribute("items", this.inventoryService.getInventory())
                .modelAttribute("cart", this.inventoryService.getCart("My Cart").defaultIfEmpty(new Cart("My Cart")))
                .build());
    }

    @PostMapping("/add/{id}")
    Mono<String> addToCart(@PathVariable String id) {
        return this.inventoryService.addItemToCart("My Cart", id).thenReturn("redirect:/");
    }

    @DeleteMapping("/remove/{id}")
    Mono<String> removeFromCart(@PathVariable String id) {
        return this.inventoryService.removeOneFromCart("My Cart", id).thenReturn("redirect:/");
    }

    @PostMapping
    Mono<String> createItem(@ModelAttribute Item newItem) {
        return this.inventoryService.saveItem(newItem).thenReturn("redirect:/");
    }

    @DeleteMapping("/delete/{id}")
    Mono<String> deleteItem(@PathVariable String id) {
        return this.inventoryService.deleteItem(id).thenReturn("redirect:/");
    }

    @GetMapping("/search")
    public Mono<Rendering> search(@RequestParam(required = false) String name,
                                  @RequestParam(required = false) String description,
                                  @RequestParam boolean useAnd) {
        return Mono.just(Rendering.view("home.html")
                .modelAttribute("items", inventoryService.searchByExample(name, description, useAnd))
                .modelAttribute("cart", this.inventoryService.getCart("My Cart").defaultIfEmpty(new Cart("My Cart")))
                .build());
    }
}

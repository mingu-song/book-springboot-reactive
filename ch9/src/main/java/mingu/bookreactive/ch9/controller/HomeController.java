package mingu.bookreactive.ch9.controller;

import mingu.bookreactive.ch9.config.SecurityConfig;
import mingu.bookreactive.ch9.entity.Cart;
import mingu.bookreactive.ch9.entity.Item;
import mingu.bookreactive.ch9.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

    private static String cartName(Authentication auth) {
        return auth.getName() + "'s Cart";
    }

    @GetMapping
    public Mono<Rendering> homo(Authentication auth) {
        return Mono.just(Rendering.view("home.html")
                .modelAttribute("items", this.inventoryService.getInventory())
                .modelAttribute("cart", this.inventoryService.getCart(cartName(auth)).defaultIfEmpty(new Cart(cartName(auth))))
                .modelAttribute("auth", auth)
                .build());
    }

    @PostMapping("/add/{id}")
    Mono<String> addToCart(Authentication auth, @PathVariable String id) {
        return this.inventoryService.addItemToCart(cartName(auth), id).thenReturn("redirect:/");
    }

    @DeleteMapping("/remove/{id}")
    Mono<String> removeFromCart(Authentication auth, @PathVariable String id) {
        return this.inventoryService.removeOneFromCart(cartName(auth), id).thenReturn("redirect:/");
    }

    // 메소드 수준 보안 테스트
    @PreAuthorize("hasRole('" + SecurityConfig.INVENTORY + "')")
    @PostMapping
    @ResponseBody
    public Mono<ResponseEntity<?>> createItem(@RequestBody Item newItem) {
        return this.inventoryService.saveItem(newItem)
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());
    }
}

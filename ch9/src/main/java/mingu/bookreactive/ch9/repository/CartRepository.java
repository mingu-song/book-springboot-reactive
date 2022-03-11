package mingu.bookreactive.ch9.repository;

import mingu.bookreactive.ch9.entity.Cart;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CartRepository extends ReactiveCrudRepository<Cart, String> {
}

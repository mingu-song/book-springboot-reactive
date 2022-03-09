package mingu.bookreactive.ch1to7.repository;


import mingu.bookreactive.ch1to7.entity.Cart;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CartRepository extends ReactiveCrudRepository<Cart, String> {
}

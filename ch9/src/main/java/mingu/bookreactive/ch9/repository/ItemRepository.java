package mingu.bookreactive.ch9.repository;

import mingu.bookreactive.ch9.entity.Item;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ItemRepository extends ReactiveCrudRepository<Item, String> {
    Mono<Item> findByName(String name);
    Mono<Void> deleteAllByName(String name);
}

package mingu.bookreactive.ch1to7.repository;

import mingu.bookreactive.ch1to7.entity.Item;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ItemRepository extends ReactiveCrudRepository<Item, String>, ReactiveQueryByExampleExecutor<Item> {
}

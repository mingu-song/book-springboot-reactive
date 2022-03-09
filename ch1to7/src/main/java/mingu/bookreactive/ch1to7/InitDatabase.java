package mingu.bookreactive.ch1to7;

import mingu.bookreactive.ch1to7.entity.Item;
import mingu.bookreactive.ch1to7.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
public class InitDatabase {
    private final Logger log = LoggerFactory.getLogger(InitDatabase.class);

    private final List<Item> itemList = List.of(
            new Item("Alf alarm clock", "kids clock", 19.99),
            new Item("Smurf TV tray", "kids TV tray", 24.99)
    );

    @Bean
    public ApplicationRunner initItems(ItemRepository itemRepository) {
        return args -> {
            itemRepository
                    .deleteAll()
                    .thenMany(Flux.just(itemList).flatMap(itemRepository::saveAll))
                    .thenMany(itemRepository.findAll())
                    .subscribe(item -> log.info(item.toString()));
        };
    }
}

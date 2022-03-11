package mingu.bookreactive.ch9.repository;

import mingu.bookreactive.ch9.entity.User;
import org.springframework.data.repository.CrudRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends CrudRepository<User, String> {

    Mono<User> findByName(String name);
}

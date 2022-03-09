package mingu.bookreactive.ch1to7.repository;

import mingu.bookreactive.ch1to7.entity.HttpTraceWrapper;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HttpTraceWrapperRepository extends MongoRepository<HttpTraceWrapper, String> {

//    Stream<HttpTraceWrapper> findAll();
//
//    void save(HttpTraceWrapper trace);
}

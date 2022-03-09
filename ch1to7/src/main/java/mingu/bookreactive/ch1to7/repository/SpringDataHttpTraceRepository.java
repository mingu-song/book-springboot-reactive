package mingu.bookreactive.ch1to7.repository;

import mingu.bookreactive.ch1to7.entity.HttpTraceWrapper;
import org.springframework.boot.actuate.trace.http.HttpTrace;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;

import java.util.List;
import java.util.stream.Collectors;

public class SpringDataHttpTraceRepository implements HttpTraceRepository {

    private final HttpTraceWrapperRepository repository;

    public SpringDataHttpTraceRepository(HttpTraceWrapperRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<HttpTrace> findAll() {
//        return repository.findAll().map(HttpTraceWrapper::getHttpTrace).collect(Collectors.toList());
        return repository.findAll().stream().map(HttpTraceWrapper::getHttpTrace).collect(Collectors.toList());
    }

    @Override
    public void add(HttpTrace trace) {
        repository.save(new HttpTraceWrapper(trace));
    }
}

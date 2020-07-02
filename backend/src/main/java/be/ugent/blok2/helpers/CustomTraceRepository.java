package be.ugent.blok2.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.trace.http.HttpTrace;
import org.springframework.boot.actuate.trace.http.HttpTraceRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class CustomTraceRepository implements HttpTraceRepository {

    List<HttpTrace> traces = new ArrayList<>();
    private final Logger loggerIcoming = LoggerFactory.getLogger("requests");

    @Override
    public List<HttpTrace> findAll() {
        return traces;
    }

    @Override
    public void add(HttpTrace trace) {
        traces.add(trace);
        loggerIcoming.info("    " + trace.getRequest().getUri() + "    " + trace.getRequest().getMethod());
    }

}

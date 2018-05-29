package io.github.svaponi.endpoint;

import io.github.svaponi.groovy.GroovyBiFunction;
import io.github.svaponi.resource.AnyResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.function.BiFunction;

@Controller
public class EndpointController {

    private final EndpointService endpointService;

    public EndpointController(final EndpointService endpointService) {
        this.endpointService = endpointService;
    }

    @RequestMapping(value = "/endpoint", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public HttpEntity<?> get() {
        return ResponseEntity.ok(endpointService.showMappings());
    }

    @RequestMapping(value = "/endpoint", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public HttpEntity<?> delete(
            @RequestParam final String url,
            @RequestParam(required = false) final String method
    ) {
        final RequestMethod requestMethod = RequestMethod.valueOf(method == null ? "GET" : method);
        endpointService.removeMapping(requestMethod, url);
        return ResponseEntity.ok(new AnyResource().withField("status", "ADDED")
                .withField("message", "endpoint registered")
                .withField("method", requestMethod)
                .withField("url", url));
    }

    @RequestMapping(value = "/endpoint", method = {RequestMethod.POST, RequestMethod.PUT}, consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public HttpEntity<?> postOrPut(
            @RequestParam final String url,
            @RequestParam(required = false) final String method,
            @RequestBody final String groovyScript
    ) {
        try {

            final RequestMethod requestMethod = RequestMethod.valueOf(method == null ? "GET" : method);
            final BiFunction<MultiValueMap, Map, ?> function = new GroovyBiFunction(groovyScript);
            endpointService.addMapping(requestMethod, url, function);

            return ResponseEntity.ok(new AnyResource().withField("status", "DELETED")
                    .withField("message", "endpoint unregistered")
                    .withField("method", requestMethod)
                    .withField("url", url));

        } catch (final IllegalArgumentException e) {

            return ResponseEntity.status(400).body(new AnyResource()
                    .withField("status", "INVALID")
                    .withField("message", e.getMessage()));

        } catch (final Exception e) {

            return ResponseEntity.status(500).body(new AnyResource()
                    .withField("status", "ERROR")
                    .withField("message", e.getMessage()));
        }
    }
}

package io.github.svaponi.jmx;

import io.github.svaponi.endpoint.EndpointService;
import io.github.svaponi.groovy.GroovyBiFunction;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;
import java.util.function.BiFunction;

@Service
public class Mapping implements MappingMBean {

    private final EndpointService endpointService;

    public Mapping(final EndpointService endpointService) {
        this.endpointService = endpointService;
    }

    @ManagedOperation(description = "Register new endpoint")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "method", description = "Endpoint HTTP method"),
            @ManagedOperationParameter(name = "url", description = "Endpoint URL"),
            @ManagedOperationParameter(name = "groovyScript", description = "Endpoint logic. Assume arguments (org.springframework.util.MultiValueMap params, Object body) are already defined."),
    })
    @Override
    public void registerEndpoint(final String method, final String url, final String groovyScript) {
        final RequestMethod requestMethod = RequestMethod.valueOf(method == null ? "GET" : method);
        final BiFunction<MultiValueMap, Map, ?> function = new GroovyBiFunction(groovyScript);
        endpointService.addMapping(requestMethod, url, function);
    }
}
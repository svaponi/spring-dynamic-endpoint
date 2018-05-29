package io.github.svaponi.jmx;

public interface MappingMBean {

    /**
     * Register new endpoint.
     *
     * @param method       Endpoint HTTP method
     * @param url          Endpoint URL
     * @param groovyScript Endpoint logic. Assume arguments (org.springframework.util.MultiValueMap params, Map body) are already defined
     */
    void registerEndpoint(final String method, final String url, final String groovyScript);
}

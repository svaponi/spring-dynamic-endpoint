package io.github.svaponi.endpoint;

import io.github.svaponi.resource.AnyResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * Add new endpoints programmatically at runtime using Groovy scripts.
 */
@Service
public class EndpointService {

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    public EndpointService(
            final RequestMappingHandlerMapping requestMappingHandlerMapping
    ) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    public Collection<AnyResource> showMappings() {
        return requestMappingHandlerMapping.getHandlerMethods().entrySet().stream()
                .map(e -> new AnyResource()
                        .withField("urls", e.getKey().getPatternsCondition().getPatterns())
                        .withField("methods", e.getKey().getMethodsCondition().getMethods())
                        .withField("entry-point", format("%s#%s(%s)",
                                e.getValue().getBeanType().getName(), // controller class name
                                e.getValue().getMethod().getName(), // controller method name
                                Arrays.stream(e.getValue().getMethod().getParameterTypes()).map(Class::getSimpleName).collect(Collectors.joining(","))) // controller method arguments
                        )
                        .withField("return-type", e.getValue().getReturnType().getParameterType().getName())
                )
                .collect(Collectors.toList());
    }

    public void removeMapping(final RequestMethod requestMethod, final String url) {
        requestMappingHandlerMapping.unregisterMapping(RequestMappingInfo.paths(url).methods(requestMethod).build());
    }

    public <T> void addMapping(final RequestMethod requestMethod, final String url, final BiFunction<MultiValueMap, Map, T> logic) {
        Assert.notNull(url, "invalid method mapping");
        Assert.hasText(url, "invalid url mapping");

        final RequestMappingInfo requestMappingInfo = RequestMappingInfo
                .paths(url)
                .methods(requestMethod)
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .build();

        final TemplateController<T> templateController = new TemplateController(logic);
        final Method templateMethod;
        try {
            templateMethod = templateController.getClass().getDeclaredMethod("handleRequest",
                    MultiValueMap.class, // query-string params
                    Map.class, // body
                    HttpServletRequest.class // http incoming request
            );
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException(format("invalid args for %s.handleRequest()", templateController.getClass()), e);
        }

        requestMappingHandlerMapping.unregisterMapping(requestMappingInfo); // removes possible old mapping before creating new one
        requestMappingHandlerMapping.registerMapping(requestMappingInfo, templateController, templateMethod);
    }

    /**
     * Controller template without any URL mapping. Used to register new endpoints
     */
    public static class TemplateController<T> {

        /**
         * transforms body and query-string params into endpoint output
         */
        private final BiFunction<MultiValueMap, Map, T> function;

        public TemplateController(final BiFunction<MultiValueMap, Map, T> function) {
            this.function = function;
        }

        public HttpEntity<?> handleRequest(
                @RequestParam final MultiValueMap params,
                @RequestBody(required = false) final Map body,
                final HttpServletRequest request) {

            final T content = function.apply(params, body);

            return ResponseEntity.ok(new AnyResource()
                    .withField("content", content)
                    .withField("method", request.getMethod())
                    .withField("uri", request.getRequestURI())
            );
        }
    }
}
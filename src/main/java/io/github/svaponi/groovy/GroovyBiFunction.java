package io.github.svaponi.groovy;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.MetaMethod;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

public class GroovyBiFunction<T> implements BiFunction<MultiValueMap, Map, T> {

    private static final GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
    private final GroovyObject groovyObj;

    /**
     * @param script body of the method to execute. Assume arguments (org.springframework.util.MultiValueMap params, Object body) are already defined.
     */
    public GroovyBiFunction(final String script) {
        try {
            final String fullClassScript = String.format("class Script_%s{Object apply(org.springframework.util.MultiValueMap params,Object body){ %s }}", UUID.randomUUID().toString().replaceAll("[-]", ""), script);
            System.out.println(fullClassScript);
            this.groovyObj = (GroovyObject) groovyClassLoader.parseClass(fullClassScript).newInstance();
            final boolean hasApplyMethod = groovyObj.getMetaClass().getMethods().stream().map(MetaMethod::getName).anyMatch(name -> name.equals("apply"));
            Assert.isTrue(hasApplyMethod, "invalid Groovy script: missing method apply(MultiValueMap,Object)");
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("invalid Groovy script", e);
        }
    }

    /**
     * @param params query-string parameters (by default Spring initiates a MultiValueMap)
     * @param body   request body (by default Spring initiates a Map)
     * @return
     */
    @Override
    public T apply(final MultiValueMap params, final Map body) {
        return (T) groovyObj.invokeMethod("apply", new Object[]{params, body});
    }
}